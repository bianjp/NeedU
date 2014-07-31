var async = require('async');
var crypto = require('crypto');
var db = require('../lib/db').getConnection();

var encryptPassword = function(origin, salt){
  var sha1 = crypto.createHash('sha1');
  sha1.update(origin + salt);
  return sha1.digest('hex');
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
  ],

  function(err, items){  // return array, even when insert a single document
    callback(err, items && items[0] && items[0]._id);
  });
};

module.exports = {
  findOne: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('users', callback);
      },

      function(col, callback){
        col.findOne(selector, callback);
      }
    ], callback);
  },

  insert: function(user, callback){
    async.waterfall([
      function(callback){
        db.collection('users', callback);
      },

      function(col, callback){
        col.insert(user, callback);
      }
    ],

    function(err, items){
      callback(err, items && items[0]);
    });
  },

  update: function(selector, updater, callback){
    async.waterfall([
      function(callback){
        db.collection('users', callback);
      },

      function(col, callback){
        col.update(selector, updater, callback);
      }
    ], callback);
  },

  remove: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('users', callback);
      },

      function(col, callback){
        col.remove(selector, callback);
      }
    ], callback);
  },

  signin: function(username, password, callback){
    var User = this;
    var user;
    async.waterfall([
      function(callback){
        User.findOne({username: username}, callback);
      },

      function(item, callback){
        if (!item || encryptPassword(password, item.password.salt) != item.password.identity){
          callback(true);
        }
        else {
          user = item;
          createSession(item._id, callback);
        }
      }
    ],

    function(err, sid){
      callback(err, user, sid);
    });
  },

  signup: function(doc, callback){
    var User = this;
    var user;
    async.waterfall([
      function(callback){
        User.insert(doc, callback);
      },

      function(item, callback){
        if (!item){
          callback(true);
        }
        else {
          user = item;
          createSession(item._id, callback);
        }
      }
    ],

    function(err, sid){
      callback(err, user, sid);
    });
  },

  checkPassword: function(userId, password, callback){
    this.findOne({_id: userId}, function(err, item){
      callback(err, item && encryptPassword(password, item.password.salt) == item.password.identity);
    });
  },

  validateUsername: function(username){
    if (!username){
      return '学号不能为空';
    }
    else if (!/^[a-zA-Z\d]+$/.test(username)){
      return '学号错误';
    }
    return false;
  },

  validatePassword: function(password){
    if (!password || password.length < 6){
      return '密码不能少于6位';
    }
    return false;
  },

  validateProfile: function(profile, isUpdating){
    var requiredFields = {
      'school': '学院',
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
  },

  // generate password field of user
  generatePassword: function(origin){
    var salt = crypto.randomBytes(10).toString('hex');
    return {
      identity: encryptPassword(origin, salt),
      salt: salt
    };
  }
};
