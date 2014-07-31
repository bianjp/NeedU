var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var Notification = require('../models/notification');

router.get('/notifications', function(req, res){
  Notification.find({userId: req.session.userId}, {}, function(err, items){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      res.send({
        status: 0,
        notifications: items
      });
    }
  });
});

router.delete('/notifications/all', function(req, res){
  Notification.remove({userId: req.session.userId}, function(err, result){
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
});

router.delete('/notifications/:id', function(req, res){
  var notificationId;
  try{
    notificationId = ObjectID(req.params.id);
  }
  catch(e){
    res.send({
      status: 1,
      message: '参数错误'
    });
    return;
  }

  Notification.remove({_id: notificationId, userId: req.session.userId}, function(err, numOfRemovedDocs, result){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else if (!numOfRemovedDocs){
      res.send({
        status: 2,
        message: '请求删除的对象不存在'
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
