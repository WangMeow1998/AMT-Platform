$(function (){
   $("#topBtn").click(setTop);
   $("#wonderfulBtn").click(setWonderful);
   $("#deleteBtn").click(setDelete);
});


function like(btn, entityType, entityId, entityUserId, postId) {
    // console.log(CONTEXT_PATH + "/like");

    //发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });

    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞':'赞');
            } else {
                alert(data.msg);
            }
        }
    );
}

//置顶
function setTop(){

    //发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });

    var btn = this;
    if($(btn).hasClass("btn-secondary")){
        $.post(
            CONTEXT_PATH + "/discussPost/top",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data.code == 0){
                    $("#topBtn").removeClass("btn-secondary").addClass("btn-danger");
                } else{
                    alert(data.msg);
                }
            }
        );
    } else {
        $.post(
            CONTEXT_PATH + "/discussPost/notTop",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data.code == 0){
                    $("#topBtn").removeClass("btn-danger").addClass("btn-secondary");
                } else{
                    alert(data.msg);
                }
            }
        );
    }

}



//加精
function setWonderful(){

    //发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });

    var btn = this;
    if($(btn).hasClass("btn-secondary")){
        $.post(
            CONTEXT_PATH + "/discussPost/wonderful",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data.code == 0){
                    $("#wonderfulBtn").removeClass("btn-secondary").addClass("btn-danger");
                } else{
                    alert(data.msg);
                }
            }
        );
    } else{
        $.post(
            CONTEXT_PATH + "/discussPost/notWonderful",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data.code == 0){
                    $("#wonderfulBtn").removeClass("btn-danger").addClass("btn-secondary");
                } else{
                    alert(data.msg);
                }
            }
        );
    }

}

//删除
function setDelete(){

    //发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });

    $.post(
        CONTEXT_PATH + "/discussPost/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 0){
                location.href = CONTEXT_PATH + "/index";
            } else{
                alert(data.msg);
            }
        }
    );
}