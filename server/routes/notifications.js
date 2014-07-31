var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var db = require('../lib/db').getConnection();

router.get('/notifications', function(req, res){
  async.waterfall([
    function(callback){
      db.collection('notifications', callback);
    },

    function(col, callback){
      col.find({userId: req.session.userId}).toArray(callback);
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
        notifications: items
      });
    }
  });
});

router.delete('/notifications/all', function(req, res){
  async.waterfall([
    function(callback){
      db.collection('notifications', callback);
    },

    function(col, callback){
      col.remove({userId: req.session.userId}, callback);
    }
  ],

  function(err, numOfRemovedDocs, result){
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

  async.waterfall([
    function(callback){
      db.collection('notifications', callback);
    },

    function(col, callback){
      col.remove({_id: notificationId, userId: req.session.userId}, callback);
    }
  ],

  function(err, numOfRemovedDocs, result){
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