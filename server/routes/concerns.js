var express = require('express');
var router = express.Router();
var async = require('async');
var crypto = require('crypto');
var ObjectID = require('mongodb').ObjectID;
var _ = require('underscore');
var notification = require('../lib/notification');

router.get('/concerns', function(req, res){
  var users;
  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne(req.session.userId, callback);
      users = col;
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
      users.find({
        _id: {
          $in: concerns
        }
      },
      {
        fields: {
          profile: 1
        }
      }).toArray(callback);
    },
  ], function(err, users){
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
  var users;
  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne(ObjectID(req.params.userId), callback);
      users = col;
    },

    function(item, callback){
      if (!item){
        res.send({
          status: 2,
          message: '该用户不存在'
        });
      }
      else{
        var concernedBy = _.map(item.concernedBy, function(id){
          return id.toString();
        });

        if (concernedBy.indexOf(req.session.userId.toString()) != -1){
          res.send({
            status: 5,
            message: '已关注该用户'
          });
        }
        else {
         callback();
        }
      }
    },

    function(callback){
      async.parallel([
        function(callback){
          users.update({_id: ObjectID(req.params.userId)}, {
            $addToSet: {
              concernedBy: req.session.userId
            }
          }, callback);
        },

        function(callback){
          users.update({_id: req.session.userId}, {
            $addToSet: {
              concerns: ObjectID(req.params.userId)
            }
          }, callback);
        }
      ], function(err, results){
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
            notification.informNewConcerner(req.db, req.session.userId, ObjectID(req.params.userId));
          }
      });
    }
  ], function(err){
      if (err){
        res.send({
          status: 3,
          message: '操作失败'
        });
      }
  });
});

router.delete('/concern/:userId', function(req, res){
  var users;
  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne(req.session.userId, callback);
      users = col;
    },

    function(item, callback){
      var concerns = _.map(item.concerns, function(id){
        return id.toString();
      });
      if (concerns.indexOf(req.params.userId) == -1){
        res.send({
          status: 1,
          message: '尚未关注该用户'
        });
      }
      else {
        async.parallel([
          function(callback){
            users.update({_id: req.session.userId}, {
              $pull: {
                concerns: ObjectID(req.params.userId)
              }
            }, callback);
          },

          function(callback){
            users.update({_id: ObjectID(req.params.userId)}, {
              $pull: {
                concernedBy: req.session.userId
              }
            }, callback);
          }
        ], function(err, results){
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
      }
    }
  ], function(err){
      if (err){
        res.send({
          status: 3,
          message: '操作失败'
        });
      }
  });
});

module.exports = router;