var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var notification = require('../lib/notification');
var User = require('../models/user');

router.get('/concerns', function(req, res){
  var users;
  async.waterfall([
    function(callback){
      User.findOne(req.session.userId, callback);
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '用户不存在'
        });
      }
      else if (!item.concerns.length){
        res.send({
          status: 0,
          users: []
        });
      }
      else {
        callback(null, item.concerns);
      }
    },

    function(concerns, callback){
      User.find({
        _id: {
          $in: concerns
        }
      },
      {
        fields: {
          profile: 1
        }
      }, callback);
    },
  ],

  function(err, users){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else {
      var profiles = [];
      for (var i = 0, length = users.length; i < length; i++){
        profiles[i] = users[i].profile;
        profiles[i]._id = users[i]._id;
      }

      res.send({
        status: 0,
        users: profiles
      });
    }
  });
});

router.post('/concern/:userId', function(req, res){
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

  async.waterfall([
    function(callback){
      User.findOne({_id: userId}, callback);
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '该用户不存在'
        });
      }
      else{
        var concernedBy = item.concernedBy.map(function(id){
          return id.toString();
        });

        if (concernedBy.indexOf(req.session.userId.toString()) != -1){
          res.send({
            status: 5,
            message: '已关注该用户'
          });
        }
        else {
          User.addConcern(req.session.userId, userId, callback);
        }
      }
    }
  ],

  function(err, results){
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
      notification.informNewConcerner(req.session.userId, userId);
    }
  });
});

router.delete('/concern/:userId', function(req, res){
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

  async.waterfall([
    function(callback){
      db.collection('users', callback);
    },

    function(col, callback){
      col.findOne(req.session.userId, callback);
      users = col;
    },

    function(item, callback){
      var concerns = item.concerns.map(function(id){
        return id.toString();
      });
      if (concerns.indexOf(userId.toString()) == -1){
        res.send({
          status: 1,
          message: '尚未关注该用户'
        });
      }
      else {
        User.removeConcern(req.session.userId, userId, callback);
      }
    }
  ],

  function(err){
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

module.exports = router;
