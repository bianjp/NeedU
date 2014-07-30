var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var _ = require('underscore');
var notification = require('../lib/notification');

var validateHelp = function(help){
  if (!help.title){
    return '标题不能为空';
  }
  if (help.title.length > 30){
    return '标题不能多于30个字';
  }
  if (!help.content || help.content.length < 10){
    return '内容不能少于10个字';
  }
  if (help.content.length > 300){
    return '内容不能多于300个字';
  }
  return false;
};

var getImages = function(files){
  if (!files){
    return [];
  }
  if (!(files instanceof Array)){ //转为数组统一处理
    files = [files];
  }
  var images = [];
  var fs = require('fs');

  for (var i = 0; i < files.length; i++){
    images.push('/images/' + files[i].name);
    fs.rename(files[i].path, 'public/images/' + files[i].name);
  }
  return images;
};

// add a help
router.post('/help', function(req, res){

  var error = validateHelp(req.body);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  var help = {
    createdAt: new Date(),
    createdBy: req.session.userId,
    title: req.body.title,
    content: req.body.content,
    images: [],
    tags: req.body.tags ? (req.body.tags instanceof Array ? req.body.tags : [req.body.tags]) : [],
    up: [],
    down: [],
    commentCount: 0
  };

  help.images = getImages(req.files.images);

  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.insert(help, callback);
    }
  ],

  function(err, items){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        help: items[0]
      });
      notification.informNewHelp(req.db, items[0]);
    }
  });
});

// delete a help
router.delete('/help/:helpId', function(req, res){
  var helps;
  var helpId;

  try{
    helpId = ObjectID(req.params.helpId);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.remove({
        _id: helpId,
        createdBy: req.session.userId
      }, callback);
    }
  ],

  function(err, numberOfRemovedDocs){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else if (numberOfRemovedDocs === 0){
      res.send({
        status: 2,
        message: '请求删除的信息不存在'
      });
    }
    else {
      res.send({
        status: 0
      });
    }
  });
});

// get help detail and 10 latest comments
router.get('/help/:helpId', function(req, res){
  var helpId;
  try{
    helpId = ObjectID(req.params.helpId);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.findOne({_id: helpId}, callback);
    }
  ],

  function(err, item){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else if (!item){
      res.send({
        status: 2,
        message: '请求的信息不存在'
      });
    }
    else {
      res.send({
        status: 0,
        help: item
      });
    }
  });
});

router.get('/help/:helpId/comments', function(req, res){
  var helpId;
  try{
    helpId = ObjectID(req.params.helpId);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  var query = {
    limit: parseInt(req.query.limit) || 10,
    offset: parseInt(req.query.offset) || 0
  };

  async.waterfall([
    function(callback){
      req.db.collection('comments', callback);
    },

    function(col, callback){
      col.find({helpId: helpId}, {
        sort: {
          createdAt: -1
        },
        limit: query.limit,
        skip: query.offset
      }).toArray(callback);
    }
  ],

  function(err, items){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        comments: items
      });
    }
  });
});

var getHelps = function(selector, options, db, res){
  async.waterfall([
    function(callback){
      db.collection('helps', callback);
    },

    function(col, callback){
      col.find(selector, options).toArray(callback);
    }
  ],

  function(err, items){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        helps: items
      });
    }
  });
};

// get helps added by the user
router.get('/helps/user/:userId', function(req, res){
  var userId;
  try{
    userId = ObjectID(req.params.userId);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  var selector = {
    createdBy: userId
  };
  var options = {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };
  getHelps(selector, options, req.db, res);
});

// get the latest helps
router.get('/helps/latest', function(req, res){
  var selector = {};
  var options = {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };
  if (req.query.tags){
    if (req.query.tags instanceof Array){
      selector.tags = {
        $all: req.query.tags
      };
    }
    else {
      selector.tags = req.query.tags;
    }
  }
  getHelps(selector, options, req.db, res);
});

// get the helps added by the persons I concerns
router.get('/helps/concerns', function(req, res){
  var options = {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };

  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne({_id: req.session.userId}, callback);
    }
  ],

  function(err, item){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      var selector = {
        createdBy: {
          $in: item.concerns
        }
      };
      getHelps(selector, options, req.db, res);
    }
  });
});

// get the helps I have commmented
router.get('/helps/commented', function(req, res){
  async.waterfall([
    function(callback){
      req.db.collection('comments', callback);
    },

    function(col, callback){
      col.find({createdBy: req.session.userId}, {
        sort: {
          createdAt: -1
        },
        fields: {
          helpId: 1
        }
      }).toArray(callback);
    },

    function(comments, callback){
      if (!comments || !comments.length){
        res.send({
          status: 0,
          helps: []
        });
      }
      else{
        var helpIds = _.map(comments, function(comment){
          return comment.helpId.toString();
        });
        helpIds = _.uniq(helpIds);
        var ids = _.map(helpIds, function(id){
          return ObjectID(id);
        });
        var limit = parseInt(req.query.limit) || 10;
        var skip  = parseInt(req.query.offset) || 0;
        ids = ids.slice(skip, skip + limit);
        var selector = {
          _id: {
            $in: ids
          }
        };
        async.waterfall([
          function(callback){
            req.db.collection('helps', callback);
          },

          function(col, callback){
            col.find(selector).toArray(callback);
          }
        ],

        function(err, helps){
          if (err){
            callback(err);
          }
          else{
            var sortedHelps = [];
            for (var i in helps){
              sortedHelps[helpIds.indexOf(helps[i]._id.toString())] = helps[i];
            }
            res.send({
              status: 0,
              helps: helps
            });
          }
        });
      }
    },

    function(callback){
      getHelps(selector, options, req.db, res);
    }
  ],

  function(err){
    res.send({
      status: 3,
      message: '操作失败'
    });
  });
});

var upDown = function(req, res, operation){
  var helpId;
  try{
    helpId = ObjectID(req.params.helpId);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  var helps;
  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.findOne({_id: helpId}, callback);
      helps = col;
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '请求操作的对象不存在'
        });
      }
      else {
        var up = item.up.map(function(id){
          return id.toString();
        });
        var down = item.down.map(function(id){
          return id.toString();
        });
        var userId = req.session.userId.toString();
        if (up.indexOf(userId) == -1 && down.indexOf(userId) == -1){
          callback();
        }
        else {
          var message;
          if (up.indexOf(userId) != -1){
            if (operation == 'up'){
              message = '已顶，不能重复操作';
            }
            else {
              message = '已顶，不能踩';
            }
          }
          else {
            if (operation == 'up'){
              message = '已踩，不能顶';
            }
            else {
              message = '已踩，不能重复操作';
            }
          }

          res.send({
            status: 5,
            message: message
          });
        }
      }
    },

    function(callback){
      var update = { $addToSet: {} };
      update.$addToSet[operation] = req.session.userId;
      helps.update({_id: helpId}, update, callback);
    }
  ],

  function(err, result){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0
      });
    }
  });
};

router.put('/help/:helpId/up', function(req, res){
  upDown(req, res, 'up');
});

router.put('/help/:helpId/down', function(req, res){
  upDown(req, res, 'down');
});

module.exports = router;
