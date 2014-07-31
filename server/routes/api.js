var express = require('express');
var router = express.Router();
var async = require('async');
var ObjectID = require('mongodb').ObjectID;
var db = require('../lib/db').getConnection();

// check sessions
router.use(function(req, res, next){
  res.type('json'); //set Content-Type to json

  if (!req.query.sid){
    delete req.session;
    next();
  }
  else {
    var sid;
    try{
      sid = ObjectID(req.query.sid);
    }
    catch(e){
      res.send({
        status: 1,
        message: 'Session ID错误'
      });
      return;
    }

    async.waterfall([
      function(callback){
        db.collection('sessions', callback);
      },

      function(col, callback){
        col.findOne(sid, callback);
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
router.use('/', require('./comments'));
router.use('/', require('./notifications'));

router.use(function(req, res, next){
  res.send({
    status: 4,
    message: '没有请求的API'
  });
});

module.exports = router;