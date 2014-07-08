var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;

// check sessions
router.use(function(req, res, next){
  res.type('json'); //set Content-Type to json

  if (!req.query.sid){
    delete req.session;
    next();
  }
  else {
    async.waterfall([
      function(callback){
        req.db.collection('sessions', callback);
      },

      function(col, callback){
        // must convert string to ObjectID
        col.findOne(ObjectID(req.query.sid), callback);
      }
    ], function(err, item){
        if (err || !item){
          delete req.session;
        }
        else {
          req.session = item;
        }
        next();
    });
  }
});

router.use(function(req, res, next){
  if (req.session){
    next();
  }
  else {
    if (req.method == 'POST' && (req.path == '/user/authentication' 
      || req.path == '/user/authentication/' || req.path == '/user' || req.path == '/user/')){
      next();
    }
    else{
      res.send({
        status: -2,
        message: '未登录'
      });
    }
  }
});


router.use('/', require('./user'));
router.use('/', require('./concerns'));
router.use('/', require('./helps'));

module.exports = router;