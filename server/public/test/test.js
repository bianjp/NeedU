$(function(){
	$('#reset').on('click', function(){
		$('input, select, textarea').not('#sid').val('');
	});

	$('#submit').on('click', function(){
		var method = $('#method').val();
		var url = $('#url').val();
		var sid = $('#sid').val();
		var body = $('#body').val();

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

			return body.replace(/[ \n]+/, '&');
		};


		$('#result textarea').val('');

		$.ajax({
			url: url,
			type: method,
			data: method == 'GET' ? null : getRequestBody(),
			dataType: 'text',
			error: function(jqXHR, textStatus, errorThrown){
				$('#result textarea').val('请求失败: ' + textStatus);
			},
			success: function(data, textStatus){
				$('#result textarea').val('请求成功：\n' + data);
				data = JSON.parse(data);
				console.log(data);
				if (data.sid){
					$('#sid').val(data.sid);
				}
			}
		});
	});

});