var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var notification = require('../lib/notification');

router.get('/comment/:commentId', function(req, res){
  var commentId;
  try{
    commentId = ObjectID(req.params.commentId);
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
      col.findOne({_id: commentId}, callback);
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
        message: '请求的对象不存在'
      });
    }
    else {
      res.send({
        status: 0,
        comment: item
      });
    }
  });
});

router.post('/comment/help/:helpId', function(req, res){
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

  var comment = {
    createdAt: new Date(),
    createdBy: req.session.userId,
    helpId: helpId,
    commentId: null,
    content: req.body.content,
    secret: !!parseInt(req.body.secret),
    thanked: false
  };
  if (req.body.commentId){
    try{
      comment.commentId = ObjectID(req.body.commentId);
    }
    catch(e){
      res.send({
        status: 1,
        message: '参数错误'
      });
      return;
    }
  }

  async.waterfall([
    function(callback){
      req.db.collection('comments', callback);
    },

    function(col, callback){
      col.insert(comment, callback);
    }
  ],

  function(err, items){
    if (err || !items){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        comment: items[0]
      });
      notification.informNewComment(req.db, items[0]);
      if (items[0].commentId){
        notification.informNewReply(req.db, items[0]);
      }
    }
  });
});

router.delete('/comment/:commentId', function(req, res){
  try{
    var commentId = ObjectID(req.params.commentId);
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
      req.db.collection('comments', callback);
    },

    function(col, callback){
      col.remove({_id: commentId}, callback);
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

router.post('/comment/:commentId/thanks', function(req, res){
  try{
    var commentId = ObjectID(req.params.commentId);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  var comments;
  var comment;

  async.waterfall([
    function(callback){
      req.db.collection('comments', callback);
    },

    function(col, callback){
      col.findOne({_id: commentId}, callback);
      comments = col;
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '请求操作的对象不存在'
        });
      }
      else if (item.thanked){
        res.send({
          status: 5,
          message: '已感谢'
        });
      }
      else {
        comment = item;
        req.db.collection('helps', callback);
      }
    },

    function(col, callback){
      col.findOne({
        _id: comment.helpId,
        createdBy: req.session.userId
      }, callback);
    },

    function(item, callback){
      if (!item){
        res.send({
          status: -1,
          message: '只有求助信息发布者可对评论者感谢'
        });
      }
      else {
        comments.update({_id: commentId}, {
          $set: {
            thanked: true
          }
        }, callback);
      }
    }
  ],

  function(err, results){
    if (err || !results){
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
});

module.exports = router;