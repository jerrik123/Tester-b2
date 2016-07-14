/**
 * 远程调用统一架构，功能包括：前台校验，前台计算，前台根据规则取值 刘春元，2012-11-26 事件事派发校验与取值 在元素上添加 validation
 * 属性 则是添加校验 在元素上添加 transform 属性 则是添加取值方式 同Name对像取值只要加一个transform，用逗号分割
 */
var $remoteCall = {
	// 得到用户的值
	getSubmitValue : function(classId) {
		var submitList = $("." + classId);
		var toServerJson = new Object();
		for (var i = 0; i < submitList.length; i++) {
			var vlist = $(submitList[i]).attr("validation");
			// 类型转换
			var name = $(submitList[i]).attr("name");
			if (!toServerJson[name]) {
				var val = $getSubmitObjVal(submitList[i]);
				toServerJson[name] = val;
			}
			if (validationProcess(vlist, submitList[i], toServerJson[name]))
				return "<false^_^>";
		}
		return toServerJson;
	},
	submitClient : function(p) {
		if (p.submitKey && !p.toServerJson) {
			p.toServerJson = this.getSubmitValue(p.submitKey);
			if ("<false^_^>" == p.toServerJson) {
				return;
			}
		}

		if (!p.classID) {
			alert("error:param classID is null");
			return false;
		}
		if (!p.toServerJson) {
			p.toServerJson = new Object();
		} 
  
		p.toServerJson.methodName = p.methodName;
		p.toServerJson.classID = p.classID; 
		window.location.href='';
		/**$.post(getContextPath() + "/webmg.action", p.toServerJson, function() {
			// 通一异常处理
			if ($.isFunction($exception[response.resultCode])) {
				if (!$exception[response.resultCode](response)) {
					return false;
				}
			}
			// 回调方法
			if (p.Function) {
				p.Function(response);
			}
		}, "html");**/
	}
}
var validationProcess = function(vlist, obj, val) {

	if (null != vlist) {
		var validation = vlist.split(",");
		for (var i = 0; i < validation.length; i++) {
			if ($.isFunction($validation[$.trim(validation[i])])) {
				if ($validation[$.trim(validation[i])](obj, val)) {
					return true;
				}
			}
		}

	}
}
var $getSubmitObjVal = function(tag) {
	var transform = $(tag).attr("transform");
	var tagName = tag.nodeName;
	if (transform) {
		if ($.isFunction($transform[$.trim(transform)])) {
			return $transform[$.trim(transform)](tag);
		}
	} else {
		// 标签取值

		if ($.isFunction($transform[tagName])) {
			return $transform[tagName](tag);
		}
	}
	var list = $(tagName + "[name=" + $(tag).attr("name") + "]");
	var result = "";
	for (var i = 0; i < list.length; i++) {
		if ("" == result) {
			if ("checkbox" == $(list[i]).attr("type")) {
				if ("checked" == $(list[i]).attr("checked")) {
					result = $(list[i]).val();
				}
			} else {
				result = $(list[i]).val();
			}
		} else {
			if ("checkbox" == $(list[i]).attr("type")) {
				if ("checked" == $(list[i]).attr("checked")) {
					result += "," + $(list[i]).val();
				}
			} else {
				result += "," + $(list[i]).val();
			}

		}
	}
	return result;
}
/**
 * 远程绑定
 */
$remoteBindProcess = {
	bind : function() {debugger;
	
		for ( var classKey in $remoteBind) {
			$remoteBind[classKey]();
		}
	}
};

$(function() {

	var IeMsg = "请使用ie8以上或者其他浏览器";
	var flag = true;
	if (navigator.userAgent.indexOf("MSIE") > 0) {
		if (navigator.userAgent.indexOf("MSIE 6.0") > 0) {
			flag = false; 
		}
		if (navigator.userAgent.indexOf("MSIE 7.0") > 0) {
			flag = false; 
		}
		if (navigator.userAgent.indexOf("MSIE 8.0") > 0) {
			//flag = false;
		}
		if (navigator.userAgent.indexOf("MSIE 9.0") > 0) {
			 //alert("ie9");
		}
	} else {
		//flag = false;
	}
	if (!flag) {
		$(".inner").html(IeMsg) ;
	}

	$init();
	$remoteBindProcess.bind();
});
var $transform = new Object();

var $validation = new Object();

var $exception = new Object();

var $remoteBind = new Object();

var $successful = new Object();

/**
 * 远程调用事件绑定调用
 */
$remoteBind.event = function() {debugger;
	var list = $(".event");
	if (null == list)
		return;
	$.each(list, function(key, val) {
		$(val).off().on("click",function() {
			$remoteCall.submitClient({
				classID : $(val).attr("mangoId"),
				submitKey : $(val).attr("submitKey"),
				Function : function(response) {
					if ($.isFunction($successful[$.trim($(val).attr(
							"successful"))])) {
						$successful[$.trim($(val).attr("successful"))]
								(response);
					}
				}
			});
		});
		/*$(val).click(
				function() {
					$remoteCall.submitClient({
						classID : $(val).attr("mangoId"),
						submitKey : $(val).attr("submitKey"),
						Function : function(response) {
							if ($.isFunction($successful[$.trim($(val).attr(
									"successful"))])) {
								$successful[$.trim($(val).attr("successful"))]
										(response);
							}
						}
					});
				});*/
	});
}

var $init = function() {
	if(typeof(beforeload) == "function"){
		beforeload();
	}
	var list = $(".initload");
	if (null == list)
		return;
	$.each(list, function(key, val) {
		$remoteCall.submitClient({
			methodName : $(val).attr("methodName"),
			classID : $(val).attr("mangoId"),
			submitKey : $(val).attr("submitKey"),
			Function : function(response) {
				if ($
						.isFunction($successful[$.trim($(val)
								.attr("successful"))])) {
					$successful[$.trim($(val).attr("successful"))](response);
				}
			}
		});
	});
}

$transform.checkbox = function(obj) {
	var list = $("input[name='" + $(obj).attr("name") + "']:checked");
	if (null == list)
		return "";
	var result = "";
	$.each(list, function(key, val) {
		result += key == 0 ? $(val).val() : ',' + $(val).val();
	});
	return result;
}
$transform.radio = function(obj) {
	return $("input[name='" + $(obj).attr("name") + "']:checked").val();
}
$validation.notNull = function(obj) {
	if (null == $(obj).val() || $.trim($(obj).val()) == "") {
		setError(obj, "*此处为必填项!");
		return true;
	}
}
$validation.bengEndDate = function(obj) {
	var dateids = $(obj).attr("dateId")
	if (dateids)
		return;
	dateids = dateids.split(',');
	var begin = $("#" + dateids[0]).val();
	var end = $("#" + dateids[1]).val();
	if (compareDate(begin, end)) {
		setError(obj, "*结束时间不能小于开始时间!");
		return true;
	}
}

var setError = function(obj, msg) {
	if ($(obj).width() == 0) {
		alert($(obj).attr("name") + "is null");
	} else {
		var error = document.createElement("div");
		$(obj).parent().append($(error));
		$(error).addClass("error_div");
		$(error).css("top", $(obj).position().top);
		$(error).css("left", $(obj).position().left);
		var width = $(obj).width();
		var height = $(obj).height();
		if ($(obj).width() < 100) {
			width = "100";
			height = "25";
		}
		$(error).width(width);
		$(error).height(height);
		$(error).append(msg);
		$(error).focus();
		$(error).hover(function() {
			$(error).remove();
		});
		$(error).blur(function() {
			$(error).remove();
		});
	}
}
$exception.ERRAUTH = function() {
	alert("您没有权限此操作！");
}
$exception.ES1 = function() {
	window.top.location.reload();
}
function getContextPath() {
	var contextPath = document.location.pathname;
	if (contextPath.substr(0, 1) != "/") {
		contextPath = "/" + contextPath;
	}
	var index = contextPath.substr(1).indexOf("/");
	contextPath = contextPath.substr(0, index + 1);
	delete index;
	return contextPath;
}
