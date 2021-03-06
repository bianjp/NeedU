$(function(){
	$('#reset').on('click', function(){
		$('input, select, textarea').not('#sid').val('');
	});

	var apis = {
		signup: {
			method: 'POST',
			url: '/user',
			body: 'username=\npassword=\nschool=\nmajor=\nschoolYear=\nname=\ngender=\nbirthday=\nphone=\nwechat=\nQQ=\ndescription='
		},
		signin: {
			method: 'POST',
			url: '/user/authentication',
			body: 'username=\npassword='
		},
		updatePassword: {
			method: 'PUT',
			url: '/user/password',
			body: 'oldPassword=\npassword='
		},
		updatePhoto: {
			method: 'PUT',
			url: '/user/photo',
			body: '',
			hasFile: true
		},
		updateProfile: {
			method: 'PUT',
			url: '/user',
			body: 'school=\nmajor=\nschoolYear=\nname=\ngender=\nbirthday=\nphone=\nwechat=\nQQ=\ndescription='
		},
		getProfile: {
			method: 'GET',
			url: '/user/',
			body: ''
		},

		getConcerns: {
			method: 'GET',
			url: '/concerns',
			body: ''
		},
		addConcern: {
			method: 'POST',
			url: '/concern/',
			body: ''
		},
		removeConcern: {
			method: 'DELETE',
			url: '/concern/',
			body: ''
		},

		addHelp: {
			method: 'POST',
			url: '/help',
			body: 'title=\ncontent=\ntags=\n',
			hasFile: true
		},
		removeHelp: {
			method: 'DELETE',
			url: '/help/',
			body: ''
		},
		getHelp: {
			method: 'GET',
			url: '/help/',
			body: ''
		},
		getComments: {
			method: 'GET',
			url: '/help//comments',
			body: 'limit=\noffset='
		},
		getHelpsByUser: {
			method: 'GET',
			url: '/helps/user/',
			body: 'limit=\noffset='
		},
		getLatestHelps: {
			method: 'GET',
			url: '/helps/latest',
			body: 'limit=\noffset=\ntags='
		},
		getConcernsHelps: {
			method: 'GET',
			url: '/helps/concerns',
			body: 'limit=\noffset='
		},
		getCommentedHelps: {
			method: 'GET',
			url: '/helps/commented',
			body: 'limit=\noffset='
		},
		upHelp: {
			method: 'PUT',
			url: '/help//up',
			body: ''
		},
		downHelp: {
			method: 'PUT',
			url: '/help//down',
			body: ''
		},

		getComment: {
			method: 'GET',
			url: '/comment/',
			body: ''
		},
		addComment: {
			method: 'POST',
			url: '/comment/help/',
			body: 'commentId=\ncontent=\nsecret=\n'
		},
		removeComment: {
			method: 'DELETE',
			url: '/comment/',
			body: ''
		},
		thankCommenter: {
			method: 'POST',
			url: '/comment//thanks',
			body: ''
		},

		getNotifications: {
			method: 'GET',
			url: '/notifications',
			body: ''
		},
		removeNotification: {
			method: 'DELETE',
			url: '/notifications/',
			body: ''
		},
		removeAllNotifications: {
			method: 'DELETE',
			url: '/notifications/all',
			body: ''
		}
	};

	$('#apis li').on('click', function(event){
		var operation = $(event.target).attr('data-operation');
		$('#method').val(apis[operation].method);
		$('#url').val(apis[operation].url);
		$('#body').val(apis[operation].body);
		if (!apis[operation].hasFile){
			$('#files').val('');
		}
	});

	$('#submit').on('click', function(){
		var method = $('#method').val();
		var url = $('#url').val();
		var sid = $('#sid').val();
		var body = $('#body').val().trim();

		var hasFile = (method == 'PUT' && url == '/user/photo') ||
								  (method == 'POST' && url == '/help');
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
			var data = {};
			var i;
			if (!/^[\s]*$/.test(body)){
				var items = body.split(/[ \n]*\n[ \n]*/);
				var pair;
				for (i = 0; i < items.length; i++){
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
			}
			if (!hasFile){
				return data;
			}
			else{
				var formdata = new FormData();
				for (var key in data){
					if (data[key] instanceof Array){
						for (i = 0; i < data[key].length; i++){
							formdata.append(key, data[key][i]);
						}
					}
					else{
						formdata.append(key, data[key]);
					}
				}
				var files = $('#files')[0].files;
				if (url.indexOf('photo') != -1){
					formdata.append('photo', files[0]);
				}
				else{
					for (i = 0; i < files.length; i++){
						formdata.append('images', files[i]);
					}
				}
				return formdata;
			}
		};

		$('#result textarea').val('');

		var options = {
			url: url,
			type: method,
			data: getRequestBody(),
			dataType: 'json',
			error: function(jqXHR, textStatus, errorThrown){
				$('#result textarea').val(textStatus);
			},
			success: function(data, textStatus){
				$('#result textarea').val(JSON.stringify(data, null, '    '));
				console.log(data);
				if (data.sid){
					$('#sid').val(data.sid);
				}
			}
		};

		if (hasFile){
			options.contentType = false;
			options.processData = false;
		}

		$.ajax(options);
	});

});