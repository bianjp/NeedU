var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var _ = require('underscore');

// add a help
router.post('/help', function(req, res){

  // validate
console.log('tags: ');
console.log(req.body.tags);
  var help = {
    createdAt: new Date(),
    createdBy: req.session.userId,
    title: req.body.title,
    content: req.body.content,
    tags: req.body.tags || [],
    up: [],
    down: [],
    comments: []
  };

  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.insert(help, callback);
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
        help: items[0]
      });
    }
  });
});

// delete a help
router.delete('/help/:helpId', function(req, res){
  var helps;
  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.remove({
        _id: ObjectID(req.params.helpId),
        createdBy: req.session.userId
      }, callback);
    }
  ],

  function(err, numberOfRemovedDocs){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else if (numberOfRemovedDocs === 0){
      res.send({
        status: 4,
        message: '请求删除的信息不存在'
      });
    }
    else {
      res.send({
        status: 0
      });
    }
  });
});

// get help detail and 10 latest comments
router.get('/help/:helpId', function(req, res){
  var helpId = ObjectID(req.params.helpId);

  async.parallel({
    help: function(callback){
      async.waterfall([
        function(callback){
          req.db.collection('helps', callback);
        },

        function(col, callback){
          col.findOne({_id: helpId}, callback);
        },
      ],

      function(err, item){
        if (err){
          callback(err);
        }
        else {
          callback(null, item);
        }
      });
    },

    comments: function(callback){
      async.waterfall([
        function(callback){
          req.db.collection('comments', callback);
        },

        function(col, callback){
          col.find({helpId: helpId}, {
            sort: {
              createdAt: -1
            },
            limit: 10
          }, callback);
        }
      ],

      function(err, items){
        if (err){
          callback(err);
        }
        else {
          callback(null, items);
        }
      });
    }
  },

  function(err, result){
    if (err){
      res.send({
        status: 3,
        message: '操作失败'
      });
    }
    else if (!result.help){
      res.send({
        status: 4,
        message: '请求的信息不存在'
      });
    }
    else {
      res.send({
        status: 0,
        help: result.help,
        comments: result.comments
      });
    }
  });

});

router.get('/help/:helpId/comments', function(req, res){
  var query = {
    limit: parseInt(req.query.limit) || 10,
    offset: parseInt(req.query.offset) || 0
  };

  async.waterfall([
    function(callback){
      req.db.collection('comments', callback);
    },

    function(col, callback){
      col.find({helpId: ObjectID(req.params.helpId)}, {
        sort: {
          createdAt: -1
        },
        limit: query.limit,
        skip: query.offset
      }, callback);
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
        comments: items
      });
    }
  });
});

var getHelps = function(selector, options, res){
  async.waterfall([
    function(callback){
      req.db.collection('helps', callback);
    },

    function(col, callback){
      col.find(selector, options, callback);
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
        helps: items
      });
    }
  });
};

// get helps added by the user
router.get('/helps/user/:userId', function(req, res){
  var selector = {
    createdBy: ObjectID(req.params.userId)
  };
  var options = {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };
  getHelps(selector, options, res);
});

// get the latest helps
router.get('/helps/latest', function(req, res){
  var selector = {};
  var options = {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };
  if (req.query.tags){
    selector.tags = {
      $in: req.query.tags
    };
  }
  getHelps(selector, options, res);
});

// get the helps added by the persons I concerns
router.get('/helps/concerns', function(req, res){
  var options = {
    sort: {
      createdAt: -1
    },
    limit: parseInt(req.query.limit) || 10,
    skip: parseInt(req.query.offset) || 0
  };

  async.waterfall([
    function(callback){
      req.db.collection('users', callback);
    },

    function(col, callback){
      col.findOne({_id: req.session.userId}, callback);
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
      var selector = {
        createdBy: {
          $in: item.concerns
        }
      };
      getHelps(selector, options, res);
    }
  });
});

module.exports = router;