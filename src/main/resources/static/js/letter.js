$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");


	//发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options){
		xhr.setRequestHeader(header, token);
	});


	var toUsername = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toUsername":toUsername,"content":content},
		function (data){
			data = $.parseJSON(data) //服务器相应的数据，转换为JSON
			$("#hintBody").text(data.msg); //在提示框中显示返回消息
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//发布成功时刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}