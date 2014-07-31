var express = require('express');
var router = express.Router();
var async = require('async');
var crypto = require('crypto');
var ObjectID = require('mongodb').ObjectID;
var _ = require('underscore');
var db = require('../lib/db').getConnection();

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

var createSession = function(userId, callback){
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

var validateProfile = function(profile, isUpdating){
  var requiredFields = {
    'school': '学校',
    'major': '专业',
    'schoolYear': '入学年份',
    'name': '名称',
    'gender': '性别'
  };

  var key;
  if (isUpdating){  //update profile
    for (key in requiredFields){
      if (profile[key] !== undefined && !profile[key]){
        return requiredFields[key] + '不能为空';
      }
    }
  }
  else {
    for (key in requiredFields){
      if (!profile[key]){
        return requiredFields[key] + '不能为空';
      }
    }
  }
  return false;
};

var validateUsername = function(username){
  if (!username){
    return '学号不能为空';
  }
  else if (!/^[a-zA-Z\d]+$/.test(username)){
    return '学号错误';
  }
  return false;
};

var validatePassword = function(password){
  if (password.length < 6){
    return '密码不能少于6位';
  }
  return false;
};

//sign up
router.post('/user', function(req, res){
  var error = validateUsername(req.body.username) || validatePassword(req.body.password);
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

  error = validateProfile(user.profile);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  async.waterfall([
    function(callback){
      db.collection('users', callback);
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
      createSession(items[0]._id, callback);
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
      db.collection('users', callback);
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
        createSession(user._id, function(err, sid){
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
      col.findOne({_id: userId}, callback);
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
        var concernedBy = _.map(item.concernedBy, function(id){
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


var getUpdatedProfile = function(body){
  var update = {};
  var fields = ['school', 'major', 'schoolYear', 'name', 'gender', 'birthday', 'phone', 'wechat', 'QQ', 'description'];
  for (var i = 0; i < fields.length; i++){
    if (body[fields[i]] !== undefined){
      update['profile.' + fields[i]] = body[fields[i]];
    }
  }
  return update;
};

//update profile
router.put('/user', function(req, res){
  var error = validateProfile(req.body);
  if (error){
    res.send({
      status: 1,
      message: error
    });
    return;
  }

  var update = getUpdatedProfile(req.body, true);
  var users;

  async.waterfall([
    function(callback){
      db.collection('users', callback);
    },

    function(col, callback){
      users = col;
      col.update({_id: req.session.userId}, {
        $set: update
      }, callback);
    },

    function(numOfAffectedDocs, result, callback){
      if (numOfAffectedDocs){
        users.findOne({_id: req.session.userId}, callback);
      }
      else {
        res.send({
          status: 3,
          message: '更新失败'
        });
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
      fs.rename(file.path, 'public/photos/' + filename, function(err){
        if (err){
          callback(err);
        }
        else {
          callback(null, true);
        }
      });
    },

    function(callback){
      async.waterfall([
        function(callback){
          db.collection('users', callback);
        },

        function(col, callback){
          col.update({_id: req.session.userId}, {
            $set: {
              'profile.photo': filepath
            }
          }, callback);
        }
      ],

      function(err, result){
        if (err){
          callback(err);
        }
        else {
          callback(null, result);
        }
      });
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
  var error = validatePassword(req.body.password);
  if (error){
    res.send({
      status: 1,
      message: error
    });
  }

  var collection;
  async.waterfall([
    function(callback){
      db.collection('users', callback);
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