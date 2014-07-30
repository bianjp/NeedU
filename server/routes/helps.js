var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var _ = require('underscore');
var notification = require('../lib/notification');

// add a help
router.post('/help', function(req, res){

  // validate

  var help = {
    createdAt: new Date(),
    createdBy: req.session.userId,
    title: req.body.title,
    content: req.body.content,
    tags: req.body.tags ? (req.body.tags instanceof Array ? req.body.tags : [req.body.tags]) : [],
    up: [],
    down: [],
    commentCount: 0
  };

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

module.exports = router;