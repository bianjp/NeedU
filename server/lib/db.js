var connection;

exports.getConnection = function(callback){
  if (connection)
    callback(connection);
  else {
    var MongoClient = require('mongodb').MongoClient;
    var config = require('../config/config').database;
    var uri = 'mongodb://' + config.host + ':' + config.port + '/' + config.name;

    MongoClient.connect(uri, function(err, db) {
      if(err){
        throw new Error('连接数据库失败！');
      }
      callback(db);
    });
  }
};

//清空数据库
exports.clear = function(callback){
  exports.getConnection(function(db){
    db.dropDatabase(function(err, result){
      if (err){
        throw new Error('清空数据库失败！');
      }
      if (callback)
        callback();
    });
  });
};

//插入即使是生产模式下也需要的数据
exports.initialize = function(callback){
  exports.getConnection(function(db){

    //插入管理员帐号
    db.collection('users', function(err, col){
      if (err){
        throw new Error('获取users collection失败！');
      }

      col.insert({
        username: 'admin',
        createdAt: new Date(),
        role: 'admin',
        password: {
          indetity: '6000a683c6d7442f8209ded2f2e52d7b376e3d58',
          salt: '1811b194ae23a1f39bd0'
        }
      }, function(err){
        if (err){
          throw new Error('插入管理员账号失败');
        }

        if (callback){
          callback();
        }
      });
    
    });
  });
};