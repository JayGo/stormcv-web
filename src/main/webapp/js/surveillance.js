var effectStreamCount = 1;
var nowRawStreamId = "";
var cameraCount = 0;

// Request code
var ADD_CAMERA = 100;
var DELETE_CAMERA = 101;
var START_RAW = 102;
var END_RAW = 104
var START_EFFECT = 3;
var END_EFFRCT = 10;

// Result status
var RESULT_SUCCESS = 0;
var RESULT_FAILED = 1;

function sleep(d) {
	for(var t = Date.now(); Date.now() - t <= d;);
}

function play(playerId, clipUrl, rtmpUrl) {
	//	alert("flow player: " + rtmpUrl + "&" + clipUrl);
	flowplayer(playerId, "player/swf/flowplayer-3.2.18.swf", {
		clip: {
			url: clipUrl,
			provider: 'rtmp',
			live: true,
		},
		plugins: {
			rtmp: {
				url: 'player/swf/flowplayer.rtmp-3.2.13.swf',
				netConnectionUrl: rtmpUrl
			}
		}
	});
}

function submit(effect) {
	if(effect != null && effect != "none") {
		$.ajax({
			type: "GET",
			url: "./api/v1/camera/startEffect?effect=" + effect + "&addr=" + addr,
			dataType: "json",
			contentType: "application/json",
			async: true,
			success: function(data, textStatus, jqXHR) {
				if(data.code == 0) {
					var msgs = data.message.split(",");
					var rtmpAddr = msgs[0];
					var rtmpAddrs = rtmpAddr.split("/");
					var streamId = rtmpAddrs[rtmpAddrs.length - 1];
					initialPlayer(rtmpAddrs, streamId);
				}
			},
			error: function(data, textStatus, jqXHR) {
				alert("error:" + textStatus);
			}
		});
	}
}

function checkEffect(effectValue) {
	switch(effectValue) {
		case 'gray':

			if($("#gray-parameter-list").hasClass("hidden")) {
				$("#gray-parameter-list").removeClass("hidden");
			}

			if(!$("#canny-parameter-list").hasClass("hidden")) {
				$("#canny-parameter-list").addClass("hidden");
			}

			if(!$("#histogram-parameter-list").hasClass("hidden")) {
				$("#histogram-parameter-list").addClass("hidden");
			}
			break;

		case 'cannyEdge':

			if(!$("#gray-parameter-list").hasClass("hidden")) {
				$("#gray-parameter-list").addClass("hidden");
			}

			if($("#canny-parameter-list").hasClass("hidden")) {
				$("#canny-parameter-list").removeClass();
			}

			if(!$("#histogram-parameter-list").hasClass("hidden")) {
				$("#histogram-parameter-list").addClass("hidden");
			}
			break;

		case 'colorHistogram':

			if(!$("#gray-parameter-list").hasClass("hidden")) {
				$("#gray-parameter-list").addClass("hidden");
			}

			if(!$("#canny-parameter-list").hasClass("hidden")) {
				$("#canny-parameter-list").addClass("hidden");
			}

			if($("#histogram-parameter-list").hasClass("hidden")) {
				$("#histogram-parameter-list").removeClass();
			}
			break;

		case 'foreground_extraction':
			break;
		default:
			alert("effect not supported!");
	}
}

function addCameraToBox(rtmpAddr, topoId, effectType, parameters, id) {
	var previewImgAddr = "";

	var box = document.querySelector("#stream-box-template");

	box.content.querySelector("li").id = "box" + id;
	box.content.querySelector(".ellipsis").innerHTML = rtmpAddr;
	box.content.querySelector(".tag.ellipsis").innerHTML = effectType;
	box.content.querySelector("a").id = "player" + id;

	$("#stream-list-contentbox").append(box.content.cloneNode(true));
	
	
	var paraSection = document.querySelector("#box" + id + " .boxarea .mes");
	
	var paraItem = document.querySelector("#parameters-list-template");
	
	for(var i in parameters) {
		paraItem.content.querySelector("label").for = i + "";
		paraItem.content.querySelector("label").innerHTML = i;
		paraItem.content.querySelector("input").name = i;
		paraItem.content.querySelector("input").value = parameters[i];
		paraItem.content.querySelector("input").setAttribute("disabled", "true");
		paraSection.appendChild(paraItem.content.cloneNode(true));
	}
	
	var rtmpUrl = extractRtmpUrl(rtmpAddr, topoId);
	
	play("player" + id, topoId, rtmpUrl);
}

function initialPlayer(rtmpAddr, streamId) {

	var li = document.createElement("li");
	li.className = "video-item";
	li.id = "video-item" + streamId;

	var divMargin = document.createElement("div");
	divMargin.className = "videomargin";
	divMargin.id = "vmargin" + streamId;

	var divBox = document.createElement("div");
	divBox.className = "videobox";
	divBox.id = "videobox" + streamId;

	var span = document.createElement("div");
	span.className = "videoname";
	span.id = "video" + streamId;
	span.innerHTML = streamId + "画面";

	var a = document.createElement("a");
	a.id = "player" + streamId;
	a.style.width = "320px";
	a.style.height = "240px";

	li.appendChild(divMargin);
	li.appendChild(divBox);

	$(".videosection .video-list").append(li);

	var playerId = "player" + streamId;
}

function commitChangeParas(obj) {
	var p = obj.parentNode;
	var childs = p.children;
	$(obj).prev().removeClass("hidden");
	for(var i = 0; i < childs.length; i++) {
		if(childs[i].nodeName == "INPUT") {
			childs[i].setAttribute("disabled", "true");
		}
	}
	$(obj).addClass("hidden");
	alert("参数修改已提交！");
}

function changeParas(obj) {
	var p = obj.parentNode;
	var childs = p.children;
	$(obj).next().removeClass("hidden");
	for(var i = 0; i < childs.length; i++) {
		if(childs[i].nodeName == "INPUT") {
			childs[i].removeAttribute("disabled");
		}
	}
	$(obj).addClass("hidden");
}

function deleteEffect(obj) {
	var p = obj.parentNode;
	var span = p.parentNode.getElementsByClassName("mes")[0].getElementsByClassName("mes-tit")[0].getElementsByTagName("SPAN")[0];
	var effectType = span.innerHTML;
	var boxId = p.parentNode.parentNode.getAttribute("id");
	var id = boxId.substring(3); // "box876  -> 876"  
	$.ajax({
		type: "POST",
		url: "./api/v1/camera/deleteEffect",
		dataType: "json",
		contentType: "application/json",
		data: JSON.stringify({
			"code": END_EFFRCT,
			"id": id
		}),
		async: false,
		success: function(data, textStatus, jqXHR) {
			if(data.status == RESULT_SUCCESS) {
				alert("删除处理效果成功！effect infos: " + JSON.stringify(data));
				var div = p.parentNode;
				var li = div.parentNode;
				li.remove();
			} else if(data.status == RESULT_FAILED) {
				alert("添加失败! effect infos: " + JSON.stringify(data));
			} else {
				alert("未知错误: " + data.status);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("error:" + textStatus);
		}
	});
}

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
				alert("Camera list is empty!");
			} else {
				alert("all cameras infos: "+JSON.stringify(data));
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

function clearCameraBox() {
	var boxListLength = $(".play-list li").length;
	for(var i = 1; i < boxListLength; i++) {
		$("#box" + i).remove();
		effectStreamCount--;
	}

}

function updateEffectCameraBox() {
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/camera/allEffects?nowRawStreamId=" + nowRawStreamId,
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			if(data != undefined) {
				for(var i = 0; i < data.length; i++) {
					alert("effect infos: "+JSON.stringify(data[i]));
					var topoName = data[i].topoName;
					var rtmpAddr = data[i].rtmpAddress;
					var effectType = data[i].effectType;
					var effectParams = data[i].effectParams;
					var id = data[i].id;
					addCameraToBox(rtmpAddr, topoName, effectType, effectParams, id);
					effectStreamCount++;
				}
			} else {
				alert("Error:" + data.code);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("wrong in updateEffectCameraBox");
			alert("error:" + textStatus);
		}
	});
}

function updateRawCameraBox() {
	var rtmpAddr = "";
	var streamId = "";
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/camera/rawRtmp?nowRawStreamId=" + nowRawStreamId,
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			if(data.status == RESULT_SUCCESS) {
				alert("update raw infos:" + JSON.stringify(data));
				fillCameraInfoToBox(data.name, data.address, data.width, data.height, data.frameRate);
				// 如果正在播放则显示rtmp地址等信息，并让网页播放器插件进行播放。
				if(data.rtmpAddress != undefined) {
					fillRawRtmpInfoToBox(data.streamId, data.rtmpAddress)
				}
			} else {
				alert("Error code:" + data.status);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("wrong in updateRawCameraBox");
			alert("error:" + textStatus);
		}
	});
}

function getNowPageRawStreamId() {
	var pathName = window.location.pathname;
	var strs1 = new Array();
	strs1 = pathName.split("/");
	var pageName = strs1[strs1.length - 1];

	if(pageName == "")
		return pageName;

	var strs2 = new Array();
	strs2 = pageName.split(".");
	return strs2[0];
}

function extractRtmpUrl(rtmpAddr, streamId) {
	var urlLength = rtmpAddr.length - streamId.length - 1;
	return rtmpAddr.substr(0, urlLength);
}

function fillRawBox(id, name, addr, rtmpAddr, width, height, frameRate) {
	document.querySelector("#box0 span[name='name']").innerHTML = name;
	//	alert("box's name: "+ nameSpan.nodeName);

	document.querySelector("#box0 span[name='addr']").innerHTML = addr;
	document.querySelector("#box0 span[name='rtmp-addr']").innerHTML = rtmpAddr;
	document.querySelector("#box0 span[name='frame-size']").innerHTML = width + "x" + height;
	document.querySelector("#box0 span[name='frame-rate']").innerHTML = frameRate;

}

function fillRawRtmpInfoToBox(streamId, rtmpAddr) {
	document.querySelector("#box0 span[name='rtmp-addr']").innerHTML = rtmpAddr;
	var rtmpUrl = extractRtmpUrl(rtmpAddr, streamId);
	play("player0", streamId, rtmpUrl);
}

function clearRawRtmpInfoInBox() {
	document.querySelector("#box0 span[name='rtmp-addr']").innerHTML = "";
}

function fillCameraInfoToBox(name, addr, width, height, frameRate) {
	document.querySelector("#box0 span[name='name']").innerHTML = name;
	document.querySelector("#box0 span[name='addr']").innerHTML = addr;
	document.querySelector("#box0 span[name='frame-size']").innerHTML = width + "x" + height;
	document.querySelector("#box0 span[name='frame-rate']").innerHTML = frameRate;
}

jQuery(document).ready(function($) {

//	initCameraList();
//	var resId = getNowPageRawStreamId();
//	if(resId != "" && resId != "index") {
//		nowRawStreamId = resId;
//		updateRawCameraBox();
//			updateEffectCameraBox();
//	}
	$("#raw-info").popover({
		trigger: 'hover',
		placement: 'bottom', //top, bottom, left or right
		title: '详细信息',
		html: 'true',
		content: '<h4>原始内容</h4>'
	});

//	$("#raw-info").data('bs.popover').options.content = '<h4>改变的内容</h4>';

	$("#close").click(function() {
		clearCameraBox();
	});

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
								alert("Raw info: " + JSON.stringify(data));
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

	$("#add-effect-dialog").dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: {
			"添加": function() {
				var effectType = $("#check-effect").val();
				var paraUl = $("#add-effect-dialog ul:not(.hidden)");
				var paraNum = $("#add-effect-dialog ul:not(.hidden) li").length;
				var paraDic = {};

				var paraLis = paraUl.children("li");
				for(var i = 0; i < paraLis.length; i++) {
					var childs = paraLis[i].children;
					var input;
					for(var j = 0; j < childs.length; j++) {
						if(childs[j].nodeName == "INPUT") {
							input = childs[j];
							break;
						}
					}
					var key = input.name;
					var val = input.value;
					paraDic[key] = val;
				}

				$.ajax({
					type: "POST",
					url: "./api/v1/camera/addEffect",
					dataType: "json",
					contentType: "application/json",
					data: JSON.stringify({
						"code": START_EFFECT,
						"streamId": nowRawStreamId,
						"effectType": effectType,
						"effectParams": paraDic
					}),
					async: false,
					success: function(data, textStatus, jqXHR) {
						if(data.status == RESULT_SUCCESS) {
							alert("添加处理效果成功！effect infos: " + JSON.stringify(data));
							var rtmpAddr = data.rtmpAddress
							var id = data.id;
							var topoName = data.topoName;
							addCameraToBox(rtmpAddr, topoName, effectType, paraDic, id);
							effectStreamCount++;
						} else if(data.status == RESULT_FAILED) {
							alert("添加失败！effect infos:" + JSON.stringify(data));
						} else {
							alert("失败未知！Error code:" + data.status);
						}
					},
					error: function(data, textStatus, jqXHR) {
						alert("error:" + textStatus);
					}
				});

				$(this).dialog("close");
			},
			"取消": function() {
				$(this).dialog("close");
			}
		}
	});
	// 添加效果按钮，弹出效果选择对话框
	$("#add-effect").click(function() {
			$("#add-effect-dialog").dialog("open");
		}

	);
	$("#add-effect-dialog #check-effect").change(function() {
		checkEffect($("#check-effect").val());
	});

	// 添加按钮，目的是添加摄像头，同../js/index.js的功能
	$("#add-raw").click(function() {
		$("#add-raw-dialog").dialog("open");
	});

	// 开始播放按钮，目的是播放原生视频
	$("#start_raw").click(function() {
		$.ajax({
			type: "POST",
			url: "./api/v1/camera/startRaw",
			dataType: "json",
			contentType: "application/json",
			data: JSON.stringify({
				"code": START_RAW,
				"streamId": nowRawStreamId
			}),
			async: false,
			success: function(data, textStatus, jqXHR) {
				if(data.status == RESULT_SUCCESS) {
					alert("开始播放！raw infos: " + JSON.stringify(data));
					fillRawRtmpInfoToBox(data.streamId, data.rtmpAddress);
				} else if(data.status = RESULT_FAILED) {
					alert("播放失败！raw infos: " + JSON.stringify(data));
				}
			},
			error: function(data, textStatus, jqXHR) {
				alert("error:" + textStatus);
			}
		});

	});

	// 停止播放按钮，目的是停止播放当前页面原生视频
	$("#stop_raw").click(function() {
		$.ajax({
			type: "POST",
			url: "./api/v1/camera/stopRaw",
			dataType: "json",
			contentType: "application/json",
			data: JSON.stringify({
				"code": END_RAW,
				"streamId": nowRawStreamId
			}),
			async: false,
			success: function(data, textStatus, jqXHR) {
				if(data.status == RESULT_SUCCESS) {
					alert("停止播放！raw infos: " + JSON.stringify(data));
					clearRawRtmpInfoInBox();
				} else if(data.status == RESULT_FAILED) {
					alert("停止失败！raw infos： " + JSON.stringify(data));
				}
			},
			error: function(data, textStatus, jqXHR) {
				alert("error:" + textStatus);
			}
		});
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