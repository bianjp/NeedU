var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var Notification = require('../models/notification');
var Comment = require('../models/comment');
var Help = require('../models/help');

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

  Comment.findOne({_id: commentId}, function(err, item){
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

var insertComment = function(req, res, comment){
  async.waterfall([
    function(callback){
      Help.findOne({_id: comment.helpId}, callback);
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '所评论的求助信息不存在'
        });
      }
      else if (!comment.commentId){
        callback(null);
      }
      else {
        Comment.findOne({_id: comment.commentId}, function(err, item){
          if (err){
            callback(err);
          }
          else if (!item){
            res.send({
              status: 2,
              message: '所回复评论不存在'
            });
          }
          else {
            comment.replyTo = item.createdBy;
            callback(null);
          }
        });
      }
    },

    function(callback){
      Comment.insert(comment, callback);
    }
  ],

  function(err, item){
    if (err || !item){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        comment: item
      });

      Notification.informNewComment(item);
      if (item.commentId){
        Notification.informNewReply(item);
      }
    }
  });
};

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

  var commentId;
  if (req.body.commentId){
    try{
      commentId = ObjectID(req.body.commentId);
    }
    catch(e){
      res.send({
        status: 1,
        message: '参数错误'
      });
      return;
    }
  }

  if (!req.body.content || !req.body.content.trim()){
    res.send({
      status: 1,
      message: '评论不能为空'
    });
  }

  var comment = {
    createdAt: new Date(),
    createdBy: req.session.userId,
    helpId: helpId,
    commentId: null,
    replyTo: null,
    content: req.body.content.trim(),
    secret: !!parseInt(req.body.secret),
    thanked: false
  };

  insertComment(req, res, comment);
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

  var comments;
  var helpId;

  async.waterfall([
    function(callback){
      Comment.findOne({_id: commentId}, callback);
    },

    function(item, callback){
      if (item){
        helpId = item.helpId;
        Comment.remove({_id: commentId}, callback);
      }
      else {
        res.send({
          status: 2,
          message: '请求删除的信息不存在'
        });
      }
    },
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
      Help.decreaseCommentCount(helpId, function(){});
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

  var comment;

  async.waterfall([
    function(callback){
      Comment.findOne({_id: commentId}, callback);
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
        Help.findOne({
          _id: comment.helpId,
          createdBy: req.session.userId
        }, callback);
      }
    },

    function(item, callback){
      if (!item){
        res.send({
          status: -1,
          message: '只有求助信息发布者可对评论者感谢'
        });
      }
      else {
        Comment.update({_id: commentId}, {
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
