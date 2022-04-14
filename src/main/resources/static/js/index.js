$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求（POST）
	$.post(
		CONTEXT_PATH + "/discussPost/add",  //请求的URL
		{"title":title, "content":content}, //向服务器POST的数据
		function (data){
			data = $.parseJSON(data) //服务器相应的数据，转换为JSON
			$("#hintBody").text(data.msg); //在提示框中显示返回消息
			$("#hintModal").modal("show"); //显示提示框
			setTimeout(function(){ //2秒后，自动隐藏提示框
				$("#hintModal").modal("hide");
				//发布成功时刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);


}