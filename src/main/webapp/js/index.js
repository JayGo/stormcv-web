jQuery(document).ready(function($) {
	// To modify
	// 向服务器请求现有的视频列表：目的是为了填充左侧摄像头列表和中间宫格
	$.ajax({
		type: "GET",
		url: "./api/v1/camera/allCameraLists",
		dataType: "json",
		contentType: "application/json",
		async: true,
		success: function(data, textStatus, jqXHR) {
			var cameraInfosJSONStr = JSON.stringify(data);

			if (data == "") {
				alert("Camera list is empty!");
			} else {

				for (var i = 0; i < data.length; i++) {
					var name = "摄像头" + i;
					var addr = data[i].addr;
					
					// 填充左侧列表
					addCameraToList(addr, name);
				}
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("error:" + textStatus);
		}
	});

	// To modify
	// 生成一个随机的topic提交至服务器，并添加一个摄像头
	$("#add").click(function() {
		var addr = prompt("输入要添加的设备地址：");
		
		if (addr) {		
			// 提交用户输入的地址
			$.ajax({
				type: "POST",
				url: "./api/v1/camera/add",
				dataType: "json",
				contentType: "application/json",
				data: JSON.stringify({
					"addr": addr + ""
				}),
				async: false,
				success: function(data, textStatus, jqXHR) {
					if (data.result == true) {
						var name = "摄像头" + cameraLength;			    
   					    //填充左侧摄像头列表
						addCameraToList(addr, name);
					} else {
						alert("Error:" + data.message);
					}
				},
				error: function(data, textStatus, jqXHR) {
					alert("error:" + textStatus);
				}
			});
		}
		else {
			alert("Invalid Video Address!");
		}
	});

});