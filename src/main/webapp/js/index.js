var cameraCount = 0;

// Request code
var ADD_CAMERA = 100;
var DELETE_CAMERA = 101;

// Result status
var RESULT_SUCCESS = 0;
var RESULT_FAILED = 1;

function addCameraToList(streamId, name, id) {
	var item = document.querySelector("#camli-template");
	var name = "摄像头 - " + name;
	item.content.querySelector(".camlist-li").id = id;
	item.content.querySelector("span").innerHTML = name;
	item.content.querySelector(".camlist-li").setAttribute("selected", "false");
	item.content.querySelector(".camlist-li .switch-cam").href = streamId + ".html";

	$(".left-menu .leftnav-camlist .camlist-ul").append(item.content.cloneNode(true));
	cameraCount++;
}

function initCameraList() {
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/camera/allCamerasList",
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			if(data.length == 0) {
//				alert("Camera list is empty!");
			} else {
				for(var i = 0; i < data.length; i++) {
					var id = cameraCount;
					var streamId = data[i].streamId;
					var name = data[i].name;

					// 填充左侧列表
					addCameraToList(streamId, name, id);
				}
			}
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(XMLHttpRequest.status);
			alert(XMLHttpRequest.readyState);
			alert(textStatus);
			alert("errorThrown:" + errorThrown);
		}
	});
}

var qRawMap = {};
var qEffectMap = {};

function startToPollResult(streamId) {
	var qId = window.setInterval(function() {
		queryInfo(streamId)
	}, 1000);
	alert("start to poll process result! " + qId);
	qRawMap[streamId] = qId;
}

function initRawsInfos() {
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/camera/allCamerasAndRtmpsList",
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			if(data.length == 0) {
//				alert("CameraAndRtmp list is empty!");
			} else {
//				alert(JSON.stringify(data));
				for(var i = 0; i < data.length; i++) {
					var id = cameraCount;
					var streamId = data[i].streamId;
					var name = data[i].name;
				}
			}
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
//			alert(XMLHttpRequest.status);
//			alert(XMLHttpRequest.readyState);
			alert(textStatus);
			alert("errorThrown:" + errorThrown);
		}
	});
}

jQuery(document).ready(function($) {

	initCameraList();
	initRawsInfos();

	$("#add-raw-dialog").dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: {
			"添加": function() {
				var name = $("#add-raw-dialog input[name='raw-name']").val();
				var addr = $("#add-raw-dialog input[name='raw-addr']").val();
				if(name != "" && addr != "") {
					$.ajax({
						type: "POST",
						url: "./api/v1/camera/addRaw",
						dataType: "json",
						contentType: "application/json",
						data: JSON.stringify({
							"code": ADD_CAMERA,
							"name": name,
							"address": addr
						}),
						async: false,
						success: function(data, textStatus, jqXHR) {
							if(data.status == RESULT_SUCCESS) {
//								alert("Raw info: " + JSON.stringify(data));
								var id = cameraCount;
								var streamId = data.streamId;
								// 填充左侧摄像头列表
								addCameraToList(streamId, name, id);
							} else if(data.status == RESULT_FAILED) {
								alert("添加失败：该摄像头已存在！");
							} else {
								alert("Error status: " + data.status);
							}
						},
						error: function(data, textStatus, jqXHR) {
							alert("error:" + textStatus);
						}
					});
					$(this).dialog("close");
				} else {
					alert("名称和地址不能为空，请重新输入!");
				}

			},
			"取消": function() {
				$(this).dialog("close");
			},
		}
	});

	// 添加按钮，目的是添加摄像头，同../js/index.js的功能
	$("#add-raw").click(function() {
		$("#add-raw-dialog").dialog("open");
	});
});

// 删除视频
$(document).on("click",
	".left-menu .leftnav-camlist .camlist-ul .camlist-li .delete",
	function() {
		var topic = $(this).parent().attr("id");
		var srcAddr = $(this).parent().attr("text");

		$.ajax({
			type: "post",
			url: "./api/v1/camera/deleteRaw",
			dataType: "json",
			contentType: "application/json",
			data: JSON.stringify({
				"code": DELETE_CAMERA,
				"streamId": nowRawStreamId
			}),
			async: false,
			success: function(data, textStatus, jqXHR) {
				if(data.status == RESULT_SUCCESS) {
					alert("成功删除原生摄像头！raw infos：" + JSON.stringify(data));
					self.location="index.html"; 
				} else if(data.status == RESULT_FAILED) {
					alert("删除失败！raw infos：" + JSON.stringify(data));
				} else {
					alert("未知错误！raw infos：" + JSON.stringify(data));
				}

			},
			error: function(data, textStatus, jqXHR) {
				alert("error:" + textStatus);
			}
		});

		$(this).parent().remove();
	});