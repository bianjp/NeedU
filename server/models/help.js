var async = require('async');
var db = require('../lib/db').getConnection();

module.exports = {
  findOne: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('helps', callback);
      },

      function(col, callback){
        col.findOne(selector, callback);
      }
    ], callback);
  },

  find: function(selector, options, callback){
    async.waterfall([
      function(callback){
        db.collection('helps', callback);
      },

      function(col, callback){
        col.find(selector, options).toArray(callback);
      }
    ], callback);
  },

  insert: function(doc, callback){
    async.waterfall([
      function(callback){
        db.collection('helps', callback);
      },

      function(col, callback){
        col.insert(doc, callback);
      }
    ],

    function(err, items){
      callback(err, items && items[0]);
    });
  },

  update: function(selector, updater, callback){
    async.waterfall([
      function(callback){
        db.collection('helps', callback);
      },

      function(col, callback){
        col.update(selector, updater, callback);
      }
    ], callback);
  },

  remove: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('helps', callback);
      },

      function(col, callback){
        col.remove(selector, callback);
      }
    ], callback);
  },

  validateHelp: function(help){
    if (!help.title){
      return '标题不能为空';
    }
    if (help.title.length > 30){
      return '标题不能多于30个字';
    }
    if (!help.content || help.content.length < 10){
      return '内容不能少于10个字';
    }
    if (help.content.length > 300){
      return '内容不能多于300个字';
    }
    return false;
  },

  // returns -1 for down, 1 for up, 0 for not voted
  hasVoted: function(help, userId){
    userId = userId.toString();

    var up = help.up.map(function(id){
      return id.toString();
    });
    if (up.indexOf(userId) != -1){
      return 1;
    }

    var down = help.down.map(function(id){
      return id.toString();
    });
    if (down.indexOf(userId) != -1){
      return -1;
    }
    return 0;
  },

  increaseCommentCount: function(helpId, callback){
    this.update({_id: helpId}, {
      $inc: {
        commentCount: 1
      }
    }, callback);
  },

  decreaseCommentCount: function(helpId, callback){
    this.update({_id: helpId}, {
      $inc: {
        commentCount: -1
      }
    }, callback);
  }
};
