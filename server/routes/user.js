var express = require('express');
var router = express.Router();
var async = require('async');
var crypto = require('crypto');
var ObjectID = require('mongodb').ObjectID;

var generateSalt = function(){
  return crypto.randomBytes(10).toString('hex');
};

var encryptPassword = function(origin, salt){
  var sha1 = crypto.createHash('sha1');
  sha1.update(origin + salt);
  return sha1.digest('hex');
};

// generate password field of user
var generatePassword = function(origin){
  var salt = generateSalt();
  return {
    identity: encryptPassword(origin, salt),
    salt: salt
  };
};

var createSession = function(db, userId, callback){
  async.waterfall([
    function(callback){
      db.collection('sessions', callback);
    },

    function(col, callback){
      col.insert({
        userId: userId,
        createdAt: new Date()
      }, callback);
    }
  ], function(err, items){  // return array, even when insert a single document
      if (err){
        callback(err);
      }
      else {
        callback(null, items[0]._id);
      }
  });
};


//sign up
router.post('/user', function(req, res){

  // validate

  var user = {
    username: req.body.username,
    createdAt: new Date(),
    password: generatePassword(req.body.password),
    profile: {
      school: req.body.school || null,
      major: req.body.major || null,
      schoolYear: req.body.schoolYear || null,
      name: req.body.name || null,
      gender: req.body.gender,
      photo: req.body.photo || null,
      birthday: req.body.birthday || null,
      phone: req.body.phone || null,
      wechat: req.body.wechat || null,
      QQ: req.body.QQ || null,
      description: req.body.description || null,
    },
    concernedBy: [],
    concerns: []
  };

  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne({username: req.body.username}, function(err, item){
        if (err){
          callback(err);
        }
        else if (item){
          res.send({
            status: 1,
            message: '用户名已存在'
          });
        }
        else {
          col.insert(user, callback);
        }
      });
    },

    function(items, callback){
      user = items[0];
      createSession(req.db, items[0]._id, callback);
    }
  ], function(err, sid){
    if (err || !sid){
      res.send({
        status: 3,
        message: '数据库操作失败'
      });
    }
    else {
      delete user.password;
      delete user.concernedBy;
      delete user.concerns;
      res.send({
        status: 0,
        sid: sid,
        user: user
      });
    }
  });
});

//sign in
router.post('/user/authentication', function(req, res){
  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne({username: req.body.username}, callback);
    }
  ], function(err, item){
      if (err || !item || 
          encryptPassword(req.body.password, item.password.salt) != item.password.identity){
        res.send({
          status: 1,
          message: '用户名或密码错误'
        });
      }
      else {
        delete item.password;
        delete item.concernedBy;
        delete item.concerns;
        var user = item;
        createSession(req.db, user._id, function(err, sid){
          if (err){
            res.send({
              status: 3,
              message: '登录失败'
            });
          }
          else {
            res.send({
              status: 0,
              sid: sid,
              user: user
            });
          }
        });
      }
  });
});

//get profile
router.get('/user/:userId', function(req, res){
  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne(ObjectID(req.params.userId), callback);
    }
  ], function(err, item){
      if (err){
        res.send({
          status: 1,
          message: '数据库操作失败'
        });
      }
      else if (!item){
        res.send({
          status: 2,
          message: '该用户不存在'
        });
      }
      else {
        res.send(item.profile);
      }
  });
});

//update profile
router.put('/user', function(req, res){

  // validate

  var profile = {
    school: req.body.school || null,
    major: req.body.major || null,
    schoolYear: req.body.schoolYear || null,
    name: req.body.name || null,
    gender: req.body.gender,
    photo: req.body.photo || null,
    birthday: req.body.birthday || null,
    phone: req.body.phone || null,
    wechat: req.body.wechat || null,
    QQ: req.body.QQ || null,
    description: req.body.description || null,
  };

  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.update({_id: req.session.userId}, {
        $set:{
          profile: profile
        }
      }, callback);
    }
  ], function(err, result){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else{
      res.send({
        status: 0
      });
    }
  });
});

//update photo
router.put('/user/photo', function(req, res){

});

//update password
router.put('/user/password', function(req, res){
  var collection;
  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne(req.session.userId, callback);
      collection = col;
    },

    function(item, callback){
      if (encryptPassword(req.body.oldPassword, item.password.salt) != item.password.identity){
        res.send({
          status: 1,
          message: '密码错误'
        });
      }
      else{
        callback(null, collection);
      }
    },

    function(col, callback){
      // update(userId, ...) would cause error
      col.update({_id: req.session.userId}, {
        $set: {
          password: generatePassword(req.body.password)
        }
      }, callback);
    }
  ], function(err, result){  // result is num of docs affected?
      if (result){
        res.send({
          status: 0
        });
      }
      else {
        res.send({
          status: 1,
          message: '操作失败'
        });
      }
  });
});

module.exports = router;