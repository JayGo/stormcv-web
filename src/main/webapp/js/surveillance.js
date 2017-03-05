var addr = "";

function GetRequest() {
	var url = location.search; // »ñÈ¡urlÖÐ"?"·ûºóµÄ×Ö´®
	// alert(url);
	var theRequest = new Object();
	if(url.indexOf("?") != -1) {
		var str = url.substr(1);
		strs = str.split("&");
		for(var i = 0; i < strs.length; i++) {
			theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
		}
	}
	return theRequest;
}

function sleep(d) {
	for(var t = Date.now(); Date.now() - t <= d;);
}

function play(playerId, clipUrl, rtmpUrl) {
	alert("flow player: "+ rtmpUrl+"&"+clipUrl);
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
	// alert();
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
			// alert("foregroundExtraction");
			break;
		default:
			alert("no effect selected!");
	}
}

function addCameraToBox(rtmpAddr, effectStreamId, effectType, parameters, id) {
	// alert("addCameraToBox is invoked!");
	var previewImgAddr = "";

	var box = document.querySelector("#stream-box-template");

	box.content.querySelector("li").id = "box" + id;
	box.content.querySelector(".ellipsis").innerHTML = rtmpAddr + "/" + effectStreamId;
	box.content.querySelector(".tag.ellipsis").innerHTML = "处理效果: " + effectType;
	box.content.querySelector("a").id = "player" + id;

	$("#stream-list-contentbox").append(box.content.cloneNode(true));

	var paraSection = $("#box" + id + " .boxarea .mes p");
	var paraItem = document.querySelector("#parameters-list-template");

	for(var i in parameters) {
		// alert("shuchu:" + i + ":" + parameters[i]);
		paraItem.content.querySelector("label").for = i + "";
		paraItem.content.querySelector("label").innerHTML = i;
		paraItem.content.querySelector("input").name = i;
		paraItem.content.querySelector("input").value = parameters[i];
		paraItem.content.querySelector("input").setAttribute("disabled", "true");
		paraSection.append(paraItem.content.cloneNode(true));
	}
	play("player" + id, effectStreamId, rtmpAddr);
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

	// divBox.appendChild(span);
	// divBox.appendChild(a);
	// divBox.appendChild(span);

	li.appendChild(divMargin);
	li.appendChild(divBox);

	$(".videosection .video-list").append(li);

	var playerId = "player" + streamId;

	// play(playerId, rtmpAddr);
}

function commitChangeParas(obj) {
	// alert(obj.nodeName);
	var p = obj.parentNode;
	var childs = p.children;
	// alert(childs.length);
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
	// alert(obj.nodeName);
	var p = obj.parentNode;
	var childs = p.children;
	// alert(childs.length);
	$(obj).next().removeClass("hidden");
	for(var i = 0; i < childs.length; i++) {
		if(childs[i].nodeName == "INPUT") {
			childs[i].removeAttribute("disabled");
		}
	}
	$(obj).addClass("hidden");
}

function addCameraToList(streamId, id) {
	var item = document.querySelector("#camli-template");
	var name = "摄像头" + id;
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

				for(var i = 0; i < data.length; i++) {
					var id = cameraCount;
					var streamId = data[i].streamId;

					// 填充左侧列表
					addCameraToList(streamId, id);
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
		// alert("effectStreamCount: "+effectStreamCount);
	}

}

function updateEffectCameraBox() {
	// clearCameraBox();
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/camera/allEffects?nowRawStreamId=" + nowRawStreamId,
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			if(data != undefined) {
				for(var i = 0; i < data.length; i++) {
					var effectStreamId = data[i].streamId;
					var rtmpAddr = data[i].rtmpAddr;
					var effectType = data[i].effectType;
					var parameters = data[i].parameters;
					// alert(camId + "\\" + playerId + "\\" + rtmpUrl + "\\" + effectType + "\\" + parameters);
					var paraDic = data[i].parameters;
					// For jersey-moxy
					//					alert(JSON.stringify(parameters));
					//					for (var j = 0; j < parameters.entry.length; j++) {
					//						// alert(parameters.entry[j].key + " : " + parameters.entry[j].value);
					//						paraDic[parameters.entry[j].key] = parameters.entry[j].value;
					//					}
					addCameraToBox(rtmpAddr, effectStreamId, effectType, paraDic, effectStreamCount);
					effectStreamCount++;
					// alert("effectStreamCount: "+effectStreamCount);
					// sleep(1800);
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

			if(data.code == 0) {
				rtmpAddr = data.rtmpAddr;
				alert("return from server rtmpAddr: "+rtmpAddr);
				streamId = data.streamId;
				alert("rtmpAddr: " + rtmpAddr);
				alert("streamId: " + streamId);
				completeRtmpUrl = rtmpAddr + "" + streamId;
				// alert("rawRtmp is: "+rtmpAddr);
				$("#box0 h3").html(completeRtmpUrl);
			} else {
				alert("Error code:" + data.code);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("wrong in updateRawCameraBox");
			alert("error:" + textStatus);
		}
	});
	play("player0", nowRawStreamId, rtmpAddr);
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

var effectStreamCount = 1;
var nowRawStreamId = "";
var cameraCount = 0;

jQuery(document).ready(function($) {

	//addr = $(".camlist-li").css("selected","true").attr("id");

	var Request = GetRequest();
	addr = Request['addr'];

	// alert(getNowPageCamId());
	var resId = getNowPageRawStreamId();
	if(resId != "")
		nowRawStreamId = resId;
	alert("now xxxx: " + resId);

	initCameraList();
	updateRawCameraBox();
	updateEffectCameraBox();

	// addCameraToList('rtmp://live.hkstv.hk.lxdns.com/live',"摄像头"+0);

	// addCameraToBox('rtmp://live.hkstv.hk.lxdns.com/live', null, null, effectStreamCount);
	// effectStreamCount++;

	//	if(addr) {
	//		$.ajax({
	//			type: "GET",
	//			url: "./api/v1/camera/play?addr="+addr,
	//			dataType: "json",
	//			contentType: "application/json",
	//			async: false,
	//			success: function(data, textStatus, jqXHR) {
	//				if (data.result == true) {
	//					var addrs = data.message;
	//					var splitAddrs = addrs.split(",");
	//					for(var i = 0; i < splitAddrs.length; i++) {
	//						var rtmpAddr = splitAddrs[i];
	//						var rtmpAddrs = rtmpAddr.split("/");
	//						var streamId = rtmpAddrs[rtmpAddrs.length - 1];
	//						initialPlayer(rtmpAddr, streamId);
	//					}	
	//				}
	//				else {
	//					alert("Error:" + data.message);
	//				}
	//			},
	//			error: function(data, textStatus, jqXHR) {
	//				alert("error:" + textStatus);
	//			}
	//		});
	//	}
	//	
	//	$.ajax({
	//		type : "GET",
	//		url : "./api/v1/camera/allCameraLists",
	//		dataType : "json",
	//		contentType : "application/json",
	//		async : true,
	//		success : function(data, textStatus, jqXHR) {
	//			var cameraInfosJSONStr = JSON.stringify(data);
	//
	//			if (data == "") {
	//				alert("Camera list is empty!");
	//			} else {
	//
	//				for (var i = 0; i < data.length; i++) {
	//					var name = "摄像头" + i;
	//					var addr = data[i].addr;
	//
	//					// 填充左侧列表
	//					addCameraToList(addr, name);
	//				}
	//			}
	//		},
	//		error : function(data, textStatus, jqXHR) {
	//			alert("error:" + textStatus);
	//		}
	//	});

	$("#close").click(function() {
		clearCameraBox();
	});

	$("#dialog-form").dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: {
			"添加": function() {
				var effectType = $("#check-effect").val();
				var paraUl = $("#dialog-form ul:not(.hidden)");
				var paraNum = $("#dialog-form ul:not(.hidden) li").length;
				var paraDic = {};
				// --- For jersey-moxy
				// var paraDicStrForJava = "{\"entry\":[";
				// --- For jackson-json
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
					//					paraDicStrForJava += "{\"key\":\"" + key + "\",\"value\":" + val + "}";
					//					if (i < paraLis.length - 1) {
					//						paraDicStrForJava += ",";
					//					}
				}
				//				paraDicStrForJava += "]}";
				// alert(paraDicStrForJava);
				// alert(JSON.stringify(paraDic));

				$.ajax({
					type: "POST",
					url: "./api/v1/camera/addEffect",
					dataType: "json",
					contentType: "application/json",
					data: JSON.stringify({
						"code": -1,
						"rtmpAddr": "",
						"streamId": nowRawStreamId,
						"effectType": effectType,
						// "parameters": JSON.parse(paraDicStrForJava)
						"parameters": paraDic
					}),
					async: false,
					success: function(data, textStatus, jqXHR) {
						if(data.code == 0) {
							var rtmpAddr = data.rtmpAddr;
							var effectStreamId = data.streamId;
							addCameraToBox(rtmpAddr, effectStreamId, effectType, paraDic, effectStreamCount);
							effectStreamCount++;
						} else if(data.code == -1) {
							alert("添加失败：该效果已存在！");
						} else {
							alert("Error code:" + data.code);
						}
					},
					error: function(data, textStatus, jqXHR) {
						alert("error:" + textStatus);
					}
				});

				$(this).dialog("close");
			},
			Cancel: function() {
				$(this).dialog("close");
			}
		},
		close: function() {
			// allFields.val("").removeClass("ui-state-error");
		}
	});
	// 添加效果按钮，弹出效果选择对话框
	$("#add_new").click(function() {
			$("#dialog-form").dialog("open");
		}

	);
	$("#dialog-form #check-effect").change(function() {
		checkEffect($("#check-effect").val());
	});

	// 添加按钮，目的是添加摄像头，同../js/index.js的功能
	$("#add").click(function() {
		var addAddr = prompt("输入要添加的设备地址：");
		var name = "摄像头" + cameraCount;
		// 填充左侧摄像头列表

		if(addAddr) {
			// 提交用户输入的地址
			$.ajax({
				type: "POST",
				url: "./api/v1/camera/add",
				dataType: "json",
				contentType: "application/json",
				data: JSON.stringify({
					"code": -1,
					"addr": addAddr + "",
					"rtmpAddr": "",
					"streamId": ""
				}),
				async: false,
				success: function(data, textStatus, jqXHR) {
					if(data.code == 0) {
						var id = cameraCount;
						var streamId = data.streamId;
						// 填充左侧摄像头列表
						addCameraToList(streamId, id);
					} else if(data.code == -1) {
						alert("添加失败：该摄像头已存在！");
					} else {
						alert("Error code: " + data.code);
					}
				},
				error: function(data, textStatus, jqXHR) {
					alert("error:" + textStatus);
				}
			});
		} else {
			alert("Invalid Video Address!");
		}
	});

	//	$("#close").click(function() {
	//		if (addr) {
	//			$.ajax({
	//				type: "GET",
	//				url: "./api/v1/camera/close?addr=" + addr,
	//				dataType: "json",
	//				contentType: "application/json",
	//				async: true,
	//				success: function(data, textStatus, jqXHR) {
	//					alert("Info:" + data.message);
	//				},
	//				error: function(data, textStatus, jqXHR) {
	//					alert("error:" + textStatus);
	//				}
	//			});
	//		}
	//	});

	// 删除视频
	$(document).on("click",
		".left-menu .leftnav-camlist .camlist-ul .camlist-li .delete",
		function() {
			var topic = $(this).parent().attr("id");
			var srcAddr = $(this).parent().attr("text");

			$.ajax({
				type: "post",
				url: "./api/v1/camera/delete",
				dataType: "json",
				contentType: "application/json",
				data: JSON.stringify({
					"topic": topic + "",
					"addr": srcAddr + ""
				}),
				async: true,
				success: function(data, textStatus, jqXHR) {
					alert("成功删除:\n\n" + JSON.stringify(data));
				},
				error: function(data, textStatus, jqXHR) {
					alert("error:" + textStatus);
				}
			});

			$(this).parent().remove();
		});

});