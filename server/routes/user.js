var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var User = require('../models/user');

//sign up
router.post('/user', function(req, res){
  req.body.username = req.body.username && req.body.username.trim();
  var error = User.validateUsername(req.body.username) || User.validatePassword(req.body.password);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  var user = {
    username: req.body.username,
    createdAt: new Date(),
    password: User.generatePassword(req.body.password),
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

  error = User.validateProfile(user.profile);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  async.waterfall([
    function(callback){
      User.findOne({username: req.body.username}, callback);
    },

    function(item, callback){
      if (item){
        res.send({
          status: 1,
          message: '用户名已存在'
        });
      }
      else {
        User.signup(user, callback);
      }
    }
  ],

  function(err, user, sid){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
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
  User.signin(req.body.username, req.body.password, function(err, user, sid){
    if (err){
      res.send({
        status: 1,
        message: '用户名或密码错误'
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

//get profile
router.get('/user/:userId', function(req, res){
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

  User.findOne({_id: userId}, function(err, item){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else if (!item){
      res.send({
        status: 2,
        message: '该用户不存在'
      });
    }
    else {
      var concernedBy = item.concernedBy.map(function(id){
        return id.toString();
      });
      item.profile._id = item._id;
      res.send({
        status: 0,
        profile: item.profile,
        concerned: concernedBy.indexOf(req.session.userId.toString()) != -1
      });
    }
  });
});

var getUpdater = function(body){
  var updater = {};
  var fields = ['school', 'major', 'schoolYear', 'name', 'gender', 'birthday', 'phone', 'wechat', 'QQ', 'description'];
  for (var i = 0; i < fields.length; i++){
    if (body[fields[i]] !== undefined){
      updater['profile.' + fields[i]] = body[fields[i]];
    }
  }
  return {$set: updater};
};

//update profile
router.put('/user', function(req, res){
  var error = User.validateProfile(req.body, true);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  var updater = getUpdater(req.body);

  async.waterfall([
    function(callback){
      User.update({_id: req.session.userId}, updater, callback);
    },

    function(numOfAffectedDocs, result, callback){
      if (!numOfAffectedDocs){
        callback(true);
      }
      else {
        User.findOne({_id: req.session.userId}, callback);
      }
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
      item.profile._id = item._id;
      res.send({
        status: 0,
        profile: item.profile
      });
    }
  });
});

//update photo
router.put('/user/photo', function(req, res){
  if (!req.files.photo){
    res.send({
      status: 1,
      message: '请上传图片'
    });
    return;
  }
  var file = req.files.photo;
  var filename = req.session.userId.toString() + '.' + file.extension;
  var filepath = '/photos/' + filename;
  async.parallel([
    function(callback){
      var fs = require('fs');
      fs.rename(file.path, 'public/photos/' + filename, callback);
    },

    function(callback){
      User.update({_id: req.session.userId}, {
        $set: {
         'profile.photo': filepath
        }
      }, callback);
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
        status: 0,
        photo: filepath
      });
    }
  });
});

//update password
router.put('/user/password', function(req, res){
  var error = User.validatePassword(req.body.password);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  async.waterfall([
    function(callback){
      User.checkPassword(req.session.userId, req.body.oldPassword, callback);
    },

    function(result, callback){
      if (!result){
        res.send({
          status: 1,
          message: '密码错误'
        });
      }
      else {
        User.update({_id: req.session.userId}, {
          $set: {
            password: User.generatePassword(req.body.password)
          }
        }, callback);
      }
    }
  ],

  function(err, numOfAffectedDocs){
    if (numOfAffectedDocs){
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
