var express = require('express');
var router = express.Router();

// check sessions
router.use(function(req, res, next){
  res.set('Access-Control-Allow-Origin', '*');
  res.type('json'); //set Content-Type to json

  if (!req.query.sid){
    delete req.session;
    next();
  }
  else {
    req.db.collection('sessions', function(err, col){
      col.find(req.query.sid, function(err, item){
        if (err || !item){
          delete req.session;
        }
        else {
          req.session = item;
        }
        next();
      });
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

module.exports = router;