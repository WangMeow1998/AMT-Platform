$(function(){
	$(".follow-btn").click(follow);
});

function follow() {


	//发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options){
		xhr.setRequestHeader(header, token);
	});


	var btn = this;
	if($(btn).hasClass("btn-info")) {
		var entityId = $(btn).prev().val();
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3,"entityId":entityId},
			function (data){
				data = $.parseJSON(data);
				if(data.code == 0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		);
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		var entityId = $(btn).prev().val();
		// 关注TA
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3,"entityId":entityId},
			function (data){
				data = $.parseJSON(data);
				if(data.code == 0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		);
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}