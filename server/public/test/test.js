$(function(){
	$('#reset').on('click', function(){
		$('input, select, textarea').not('#sid').val('');
	});

	var apis = {
		'signup': {
			method: 'POST',
			url: '/user',
			body: 'username=\npassword=\nschool=\nmajor=\nschoolYear=\nname=\ngender=\nbirthday=\nphone=\nwechat=\nQQ=\ndescription='
		},
		'signin': {
			method: 'POST',
			url: '/user/authentication',
			body: 'username=\npassword='
		},
		'updatePassword': {
			method: 'PUT',
			url: '/user/password',
			body: 'oldPassword=\npassword='
		},
		'updatePhoto': {
			method: 'PUT',
			url: '/user/photo',
			body: ''
		},
		'updateProfile': {
			method: 'PUT',
			url: '/user',
			body: 'school=\nmajor=\nschoolYear=\nname=\ngender=\nbirthday=\nphone=\nwechat=\nQQ=\ndescription='
		},
		'getProfile': {
			method: 'GET',
			url: '/user/',
			body: ''
		},
		'getConcerns': {
			method: 'GET',
			url: '/concerns',
			body: ''
		},
		'addConcern': {
			method: 'POST',
			url: '/concern/',
			body: ''
		},
		'removeConcern': {
			method: 'DELETE',
			url: '/concern/',
			body: ''
		},
		'addHelp': {
			method: 'POST',
			url: '/help',
			body: 'title=\ncontent=\ntags=\n'
		},
		'removeHelp': {
			method: 'DELETE',
			url: '/help/',
			body: ''
		},
		'getHelp': {
			method: 'GET',
			url: '/help/',
			body: ''
		},
		'getComments': {
			method: 'GET',
			url: '/help//comments',
			body: 'limit=\noffset='
		},
		'getHelpsByUser': {
			method: 'GET',
			url: '/helps/user/',
			body: 'limit=\noffset='
		},
		'getLatestHelps': {
			method: 'GET',
			url: '/helps/latest',
			body: 'limit=\noffset=\ntags='
		},
		'getConcernsHelps': {
			method: 'GET',
			url: '/helps/concerns',
			body: 'limit=\noffset='
		}
	};

	$('#apis li').on('click', function(event){
		var operation = $(event.target).attr('data-operation');
		$('#method').val(apis[operation].method);
		$('#url').val(apis[operation].url);
		$('#body').val(apis[operation].body);
	});

	$('#submit').on('click', function(){
		var method = $('#method').val();
		var url = $('#url').val();
		var sid = $('#sid').val();
		var body = $('#body').val().trim();

		url = '/api' + url;

		if (!/^[\s]*$/.test(sid)){
			if (url.indexOf('?') == -1){
				url += '?sid=' + sid;
			}
			else {
				url += '&sid=' + sid;
			}
		}

		var getRequestBody = function(){
			if (/^[\s]*$/.test(body)){
				return null;
			}

			var items = body.split(/[ \n]+/);
			var pair;
			var data = {};
			for (var i = 0; i < items.length; i++){
				pair = items[i].split('=');
				if (!data[pair[0]]){
					data[pair[0]] = pair[1];
				}
				else if (data[pair[0]] instanceof Array){
					data[pair[0]].push(pair[1]);
				}
				else{
					data[pair[0]] = [data[pair[0]]];
					data[pair[0]].push(pair[1]);
				}
			}
			return data;
		};


		$('#result textarea').val('');

		$.ajax({
			url: url,
			type: method,
			data: getRequestBody(),
			dataType: 'text',
			error: function(jqXHR, textStatus, errorThrown){
				$('#result textarea').val(textStatus);
			},
			success: function(data, textStatus){
				data = JSON.parse(data);
				$('#result textarea').val(JSON.stringify(data, null, '    '));
				console.log(data);
				if (data.sid){
					$('#sid').val(data.sid);
				}
			}
		});
	});

});