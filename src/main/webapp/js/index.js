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

function fillRawsInfosTable(name, address, streamId, rtmpAddress, frameWidth, frameHeight, frameRate) {
	var item = document.querySelector("#raw-info-tr");
	var name = "摄像头 - " + name;
	
	item.content.querySelector("tr td[name='name']").innerHTML = name;
	item.content.querySelector("tr td[name='address']").innerHTML = address;
	item.content.querySelector("tr td[name='streamId']").innerHTML = streamId;
	item.content.querySelector("tr td[name='rtmpAddress']").innerHTML = rtmpAddress;
	item.content.querySelector("tr td[name='frameSize']").innerHTML = frameWidth + "x" + frameHeight;
	item.content.querySelector("tr td[name='frameRate']").innerHTML = frameRate;
	
	$("#raw-info-table tbody").append(item.content.cloneNode(true));
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
			} else {
				for(var i = 0; i < data.length; i++) {
					fillRawsInfosTable(data[i].name, data[i].address, data[i].streamId, data[i].rtmpAddress, data[i].width, data[i].height, data[i].frameRate);
				}
			}
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(textStatus);
			alert("errorThrown:" + errorThrown);
		}
	});
}

var popoverCompInfoTemplate = [
						'<table class="table popover">',
						    '<thead>',
							'<tr>',
								'<th>组件ID</th>',
								'<th>组件类型</th>',
								'<th>executor数目</th>',
								'<th>task数目</th>',
								'<th>总处理时间</th>',
								'<th>总失败时间</th>',
								'<th>总延迟时间</th>',
							'</tr>',
						'</thead>',
							'<tbody class="popover-content">',
							'</tbody>',
						'</table>'
						].join('');
						
						
var popoverWorkerInfoTemplate = [
						'<table class="table popover">',
						    '<thead>',
							'<tr>',
								'<th>节点</th>',
								'<th>进程号</th>',
								'<th>端口</th>',
								'<th>CPU占用</th>',
								'<th>内存占用</th>',
							'</tr>',
						'</thead>',
							'<tbody class="popover-content">',
							'</tbody>',
						'</table>'
						].join('');

						
function getCompInfoPopoverContent(componentId, type, executorNum, taskNum, allTimeProcess, allTimeFailed, allTimeLatency) {
	var effectInfoContent = [
							'<tr>',
								'<td>'+componentId+'</td>',
								'<td>'+type+'</td>',
								'<td>'+executorNum+'</td>',
								'<td>'+taskNum+'</td>',
								'<td>'+allTimeProcess+'</td>',
								'<td>'+allTimeFailed+'</td>',
								'<td>'+allTimeLatency+'</td>',
							'</tr>',].join('');
	return effectInfoContent;
}

function initTopoComponentInfoPopover(popOverId, compInfoContent) {
	$("#"+popOverId).popover({
		trigger: 'click',
		placement: 'bottom', //top, bottom, left or right
		title: '组件信息',
		html: 'true',
		template: popoverCompInfoTemplate ,
		content: compInfoContent
	});
}


function initTopoWorkerInfoPopover(popOverId, workerInfoContent) {
	$("#"+popOverId).popover({
		trigger: 'click',
		placement: 'bottom', //top, bottom, left or right
		title: '节点信息',
		html: 'true',
		template: popoverWorkerInfoTemplate ,
		content: workerInfoContent
	});
}

function getHtmlCompInfoContent(topoName) {
	var compInfoContent = "";
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/topology/topoCompInfo?topoName=" + topoName,
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			//			if(data.status == RESULT_SUCCESS) {
			for(var i = 0; i < data.length; i++) {
				compInfoContent += getCompInfoPopoverContent(data[i].componentId, data[i].type, data[i].executorNum, data[i].taskNum, data[i].allTimeProcess, data[i].allTimeFailed, data[i].allTimeLatency);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("wrong in updateRawCameraBox");
			alert("error:" + textStatus);
		}
	});
	return compInfoContent;
}

function getWorkerInfoPopoverContent(host, pid, port, cpuUsage, memoryUsage) {
	var workerInfoContent = [
							'<tr>',
								'<td>'+host+'</td>',
								'<td>'+pid+'</td>',
								'<td>'+port+'</td>',
								'<td>'+cpuUsage+'</td>',
								'<td>'+memoryUsage+'</td>',
							'</tr>',].join('');
	return workerInfoContent;
}

function getHtmlWorkerInfoContent(topoName) {
	var workerInfoContent = "";
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/topology/topoWorkerInfo?topoName=" + topoName,
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			//			if(data.status == RESULT_SUCCESS) {
			for(var i = 0; i < data.length; i++) {
				workerInfoContent += getWorkerInfoPopoverContent(data[i].host, data[i].pid, data[i].port, data[i].cpuUsage, data[i].memoryUsage);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("wrong in updateRawCameraBox");
			alert("error:" + textStatus);
		}
	});
	return workerInfoContent;
}


function fillBasicToposInofsTable(topoName, topoId, owner, workerNum, executorNum, taskNum, upTime, status) {
	var item = document.querySelector("#topo-info-tr");
	var compInfoPopoverId = topoId + "-comp-info-popover";
	var workerInfoPopoverId = topoId + "-worker-info-popover";
	
	item.content.querySelector("tr td[name='topoName']").innerHTML = topoName;
	item.content.querySelector("tr td[name='topoId']").innerHTML = topoId;
	item.content.querySelector("tr td[name='owner']").innerHTML = owner;
	item.content.querySelector("tr td[name='workerNum']").innerHTML = workerNum;
	item.content.querySelector("tr td[name='executorNum']").innerHTML = executorNum;
	item.content.querySelector("tr td[name='taskNum']").innerHTML = taskNum;
	item.content.querySelector("tr td[name='upTime']").innerHTML = upTime;
	item.content.querySelector("tr td[name='status']").innerHTML = status;
	item.content.querySelector("tr td[name='details'] button[name='comp-info-btn']").id = compInfoPopoverId;
	item.content.querySelector("tr td[name='details'] button[name='worker-info-btn']").id = workerInfoPopoverId;
	
	
	$("#topo-info-table tbody").append(item.content.cloneNode(true));
	
	var compInfoContent = getHtmlCompInfoContent(topoName);
	initTopoComponentInfoPopover(compInfoPopoverId,compInfoContent);
	
	var workerInfoContent = getHtmlWorkerInfoContent(topoName);
	initTopoWorkerInfoPopover(workerInfoPopoverId,workerInfoContent);
	
}




function initBasicToposInfos() {
	$.ajax({
		async: false,
		type: "GET",
		url: "./api/v1/camera/allBasicToposInfos",
		dataType: "json",
		contentType: "application/json",
		success: function(data, textStatus, jqXHR) {
			if(data.length == 0) {
				
			} else {
//				alert("all basic topo infos: "+JSON.stringify(data));
				for(var i = 0; i < data.length; i++) {
					fillBasicToposInofsTable(data[i].topoName, data[i].topoId, data[i].owner, data[i].workerNum, 
						data[i].executorNum, data[i].taskNum, data[i].upTimeSecs, data[i].status);
				}
			}
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(textStatus);
			alert("errorThrown:" + errorThrown);
		}
	});
}


jQuery(document).ready(function($) {

	initCameraList();
	initRawsInfos();
	initBasicToposInfos();

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
								fillRawsInfosTable(name, addr, streamId, data.rtmpAddress, data.width, data.height, data.frameRate);
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