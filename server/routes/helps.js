var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var _ = require('underscore');
var Notification = require('../models/notification');
var Help = require('../models/help');
var User = require('../models/user');
var Comment = require('../models/comment');

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

  var error = Help.validateHelp(req.body);
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

  Help.insert(help, function(err, item){
    if (err || !item){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        help: item
      });
      Notification.informNewHelp(item);
    }
  });
});

// delete a help
router.delete('/help/:helpId', function(req, res){
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

  var selector = {
    _id: helpId,
    createdBy: req.session.userId
  };
  Help.remove(selector, function(err, numberOfRemovedDocs){
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

  Help.findOne({_id: helpId}, function(err, item){
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

  Comment.find({helpId: helpId}, {
    sort: {
      createdAt: -1
    },
    limit: query.limit,
    skip: query.offset
  }, function(err, items){
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

var getOptions = function(req){
  return {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };
};

var getHelps = function(selector, options, res){
  Help.find(selector, options, function(err, items){
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

  var options = getOptions(req);
  var selector = {
    createdBy: userId
  };

  getHelps(selector, options, res);
});

// get the latest helps
router.get('/helps/latest', function(req, res){
  var selector = {};
  var options = getOptions(req);
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
  getHelps(selector, options, res);
});

// get the helps added by the persons I concerns
router.get('/helps/concerns', function(req, res){
  var options = getOptions(req);

  User.findOne({_id: req.session.userId}, function(err, item){
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
      getHelps(selector, options, res);
    }
  });
});

// get the helps I have commmented
router.get('/helps/commented', function(req, res){
  async.waterfall([
    function(callback){
      Comment.find({createdBy: req.session.userId}, {
        sort: {
          createdAt: -1
        },
        fields: {
          helpId: 1
        }
      }, callback);
    },

    function(comments, callback){
      if (!comments || !comments.length){
        res.send({
          status: 0,
          helps: []
        });
      }
      else{
        var helpIds = comments.map(function(comment){
          return comment.helpId.toString();
        });
        helpIds = _.uniq(helpIds);
        var ids = helpIds.map(function(id){
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

        Help.find(selector, {}, function(err, helps){
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
      getHelps(selector, options, res);
    }
  ],

  function(err){
    res.send({
      status: 3,
      message: '操作失败'
    });
  });
});

var vote = function(req, res, operation){
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
      Help.findOne({_id: helpId}, callback);
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '请求操作的对象不存在'
        });
      }
      else {
        var hasVoted = Help.hasVoted(item, req.session.userId);
        if (hasVoted){
          res.send({
            status: 5,
            message: hasVoted == 1 ? '已顶过' : '已踩过'
          });
        }
        else {
          var updater = { $addToSet: {} };
          updater.$addToSet[operation] = req.session.userId;
          Help.update({_id: helpId}, updater, callback);
        }
      }
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
  vote(req, res, 'up');
});

router.put('/help/:helpId/down', function(req, res){
  vote(req, res, 'down');
});

module.exports = router;
