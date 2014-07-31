var async = require('async');
var db = require('../lib/db').getConnection();
var Help = require('./help');

module.exports = {
  findOne: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('comments', callback);
      },

      function(col, callback){
        col.findOne(selector, callback);
      }
    ], callback);
  },

  find: function(selector, options, callback){
    async.waterfall([
      function(callback){
        db.collection('comments', callback);
      },

      function(col, callback){
        col.find(selector, options).toArray(callback);
      }
    ], callback);
  },

  insert: function(doc, callback){
    async.waterfall([
      function(callback){
        db.collection('comments', callback);
      },

      function(col, callback){
        col.insert(doc, callback);
      },
    ],

    function(err, items){
      callback(err, items && items[0]);
      if (items && items[0]){
        Help.increaseCommentCount(items[0].helpId, function(){});
      }
    });
  },

  update: function(selector, updater, callback){
    async.waterfall([
      function(callback){
        db.collection('comments', callback);
      },

      function(col, callback){
        col.update(selector, updater, callback);
      }
    ], callback);
  },

  remove: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('comments', callback);
      },

      function(col, callback){
        col.remove(selector, callback);
      }
    ], callback);
  },
};
