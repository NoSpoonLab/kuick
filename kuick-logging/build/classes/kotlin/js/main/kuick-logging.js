(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'kuick-logging'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'kuick-logging'.");
    }
    root['kuick-logging'] = factory(typeof this['kuick-logging'] === 'undefined' ? {} : this['kuick-logging'], kotlin);
  }
}(this, function (_, Kotlin) {
  'use strict';
  var $$importsForInline$$ = _.$$importsForInline$$ || (_.$$importsForInline$$ = {});
  var LinkedHashMap_init = Kotlin.kotlin.collections.LinkedHashMap_init_q3lmfv$;
  var toList = Kotlin.kotlin.collections.toList_7wnvza$;
  var LinkedHashSet_init = Kotlin.kotlin.collections.LinkedHashSet_init_287e2$;
  var ensureNotNull = Kotlin.ensureNotNull;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var contains = Kotlin.kotlin.collections.contains_2ws7j4$;
  var Kind_OBJECT = Kotlin.Kind.OBJECT;
  var Enum = Kotlin.kotlin.Enum;
  var throwISE = Kotlin.throwISE;
  var Kind_INTERFACE = Kotlin.Kind.INTERFACE;
  var defineInlineFunction = Kotlin.defineInlineFunction;
  var wrapFunction = Kotlin.wrapFunction;
  LogLevel.prototype = Object.create(Enum.prototype);
  LogLevel.prototype.constructor = LogLevel;
  var loggers;
  function Logger(name) {
    logInitOnce();
    var $receiver = loggers;
    var tmp$;
    var value = $receiver.get_11rb$(name);
    if (value == null) {
      var answer = LogCreate(name);
      $receiver.put_xwzc9p$(name, answer);
      tmp$ = answer;
    }
     else {
      tmp$ = value;
    }
    return tmp$;
  }
  function LoggerInit() {
    logInitOnce();
  }
  function LoggerGetLoggers() {
    return toList(loggers.values);
  }
  var logInitialized;
  function logInitOnce() {
    if (logInitialized)
      return;
    logInitialized = true;
    LogInit();
  }
  function LoggerConfig() {
    LoggerConfig_instance = this;
    logInitOnce();
    this.enabled = true;
    this.defaultMinLevel = LogLevel$WARN_getInstance();
    this.perLog_0 = LinkedHashMap_init();
  }
  function LoggerConfig$ItemConfig(logger) {
    this.logger = logger;
    this.enabled = true;
    this.enabledTags = null;
    this.disabledTags = null;
    this.minLevel = null;
  }
  LoggerConfig$ItemConfig.prototype.enableTag_61zpoe$ = function (tag) {
    if (this.enabledTags == null)
      this.enabledTags = LinkedHashSet_init();
    ensureNotNull(this.enabledTags).add_11rb$(tag);
  };
  LoggerConfig$ItemConfig.prototype.disableTag_61zpoe$ = function (tag) {
    if (this.disabledTags == null)
      this.disabledTags = LinkedHashSet_init();
    ensureNotNull(this.disabledTags).add_11rb$(tag);
  };
  LoggerConfig$ItemConfig.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ItemConfig',
    interfaces: []
  };
  LoggerConfig.prototype.getConfig_zgedc6$ = function (logger) {
    var $receiver = this.perLog_0;
    var tmp$;
    var value = $receiver.get_11rb$(logger);
    if (value == null) {
      var answer = new LoggerConfig$ItemConfig(logger);
      $receiver.put_xwzc9p$(logger, answer);
      tmp$ = answer;
    }
     else {
      tmp$ = value;
    }
    return tmp$;
  };
  LoggerConfig.prototype.enabled_d1xjht$ = function (logger, level, tag) {
    var tmp$;
    if (!this.enabled)
      return false;
    var config = this.getConfig_zgedc6$(logger);
    var configMinLevel = (tmp$ = config.minLevel) != null ? tmp$ : this.defaultMinLevel;
    if (level.compareTo_11rb$(configMinLevel) > 0)
      return false;
    var enabledTags = config.enabledTags;
    var disabledTags = config.disabledTags;
    if (disabledTags != null && contains(disabledTags, tag))
      return false;
    if (enabledTags != null && !contains(enabledTags, tag))
      return false;
    return true;
  };
  LoggerConfig.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'LoggerConfig',
    interfaces: []
  };
  var LoggerConfig_instance = null;
  function LoggerConfig_getInstance() {
    if (LoggerConfig_instance === null) {
      new LoggerConfig();
    }
    return LoggerConfig_instance;
  }
  function LogLevel(name, ordinal, index) {
    Enum.call(this);
    this.index = index;
    this.name$ = name;
    this.ordinal$ = ordinal;
  }
  function LogLevel_initFields() {
    LogLevel_initFields = function () {
    };
    LogLevel$FATAL_instance = new LogLevel('FATAL', 0, 0);
    LogLevel$ERROR_instance = new LogLevel('ERROR', 1, 1);
    LogLevel$WARN_instance = new LogLevel('WARN', 2, 2);
    LogLevel$INFO_instance = new LogLevel('INFO', 3, 3);
    LogLevel$TRACE_instance = new LogLevel('TRACE', 4, 4);
    LogLevel$Companion_getInstance();
  }
  var LogLevel$FATAL_instance;
  function LogLevel$FATAL_getInstance() {
    LogLevel_initFields();
    return LogLevel$FATAL_instance;
  }
  var LogLevel$ERROR_instance;
  function LogLevel$ERROR_getInstance() {
    LogLevel_initFields();
    return LogLevel$ERROR_instance;
  }
  var LogLevel$WARN_instance;
  function LogLevel$WARN_getInstance() {
    LogLevel_initFields();
    return LogLevel$WARN_instance;
  }
  var LogLevel$INFO_instance;
  function LogLevel$INFO_getInstance() {
    LogLevel_initFields();
    return LogLevel$INFO_instance;
  }
  var LogLevel$TRACE_instance;
  function LogLevel$TRACE_getInstance() {
    LogLevel_initFields();
    return LogLevel$TRACE_instance;
  }
  var mapCapacity = Kotlin.kotlin.collections.mapCapacity_za3lpa$;
  var coerceAtLeast = Kotlin.kotlin.ranges.coerceAtLeast_dqglrj$;
  var LinkedHashMap_init_0 = Kotlin.kotlin.collections.LinkedHashMap_init_bwtc7$;
  function LogLevel$Companion() {
    LogLevel$Companion_instance = this;
    var $receiver = LogLevel$values();
    var capacity = coerceAtLeast(mapCapacity($receiver.length), 16);
    var destination = LinkedHashMap_init_0(capacity);
    var tmp$;
    for (tmp$ = 0; tmp$ !== $receiver.length; ++tmp$) {
      var element = $receiver[tmp$];
      destination.put_xwzc9p$(element.name, element);
    }
    this.BY_NAME_0 = destination;
  }
  LogLevel$Companion.prototype.invoke_61zpoe$ = function (name) {
    return this.BY_NAME_0.get_11rb$(name.toUpperCase());
  };
  LogLevel$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var LogLevel$Companion_instance = null;
  function LogLevel$Companion_getInstance() {
    LogLevel_initFields();
    if (LogLevel$Companion_instance === null) {
      new LogLevel$Companion();
    }
    return LogLevel$Companion_instance;
  }
  LogLevel.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'LogLevel',
    interfaces: [Enum]
  };
  function LogLevel$values() {
    return [LogLevel$FATAL_getInstance(), LogLevel$ERROR_getInstance(), LogLevel$WARN_getInstance(), LogLevel$INFO_getInstance(), LogLevel$TRACE_getInstance()];
  }
  LogLevel.values = LogLevel$values;
  function LogLevel$valueOf(name) {
    switch (name) {
      case 'FATAL':
        return LogLevel$FATAL_getInstance();
      case 'ERROR':
        return LogLevel$ERROR_getInstance();
      case 'WARN':
        return LogLevel$WARN_getInstance();
      case 'INFO':
        return LogLevel$INFO_getInstance();
      case 'TRACE':
        return LogLevel$TRACE_getInstance();
      default:throwISE('No enum constant kuick.logging.LogLevel.' + name);
    }
  }
  LogLevel.valueOf_61zpoe$ = LogLevel$valueOf;
  function Logger_0() {
  }
  Logger_0.prototype.enabled_pg0dex$$default = function (level, tag) {
    return LoggerConfig_getInstance().enabled_d1xjht$(this, level, tag);
  };
  Logger_0.prototype.enabled_pg0dex$ = function (level, tag, callback$default) {
    if (tag === void 0)
      tag = null;
    return callback$default ? callback$default(level, tag) : this.enabled_pg0dex$$default(level, tag);
  };
  Logger_0.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'Logger',
    interfaces: []
  };
  function get_config($receiver) {
    return LoggerConfig_getInstance().getConfig_zgedc6$($receiver);
  }
  var log = defineInlineFunction('kuick-logging.kuick.logging.log_a2k8ig$', function ($receiver, level, tag, message) {
    if (tag === void 0)
      tag = null;
    if ($receiver.enabled_pg0dex$(level, tag))
      $receiver.log_do20u3$(level, message(), tag);
  });
  var trace = defineInlineFunction('kuick-logging.kuick.logging.trace_vkoxdw$', wrapFunction(function () {
    var LogLevel = _.kuick.logging.LogLevel;
    return function ($receiver, tag, message) {
      if (tag === void 0)
        tag = null;
      var level = LogLevel.TRACE;
      if ($receiver.enabled_pg0dex$(level, tag))
        $receiver.log_do20u3$(level, message(), tag);
    };
  }));
  var info = defineInlineFunction('kuick-logging.kuick.logging.info_vkoxdw$', wrapFunction(function () {
    var LogLevel = _.kuick.logging.LogLevel;
    return function ($receiver, tag, message) {
      if (tag === void 0)
        tag = null;
      var level = LogLevel.INFO;
      if ($receiver.enabled_pg0dex$(level, tag))
        $receiver.log_do20u3$(level, message(), tag);
    };
  }));
  var warn = defineInlineFunction('kuick-logging.kuick.logging.warn_vkoxdw$', wrapFunction(function () {
    var LogLevel = _.kuick.logging.LogLevel;
    return function ($receiver, tag, message) {
      if (tag === void 0)
        tag = null;
      var level = LogLevel.WARN;
      if ($receiver.enabled_pg0dex$(level, tag))
        $receiver.log_do20u3$(level, message(), tag);
    };
  }));
  var error = defineInlineFunction('kuick-logging.kuick.logging.error_vkoxdw$', wrapFunction(function () {
    var LogLevel = _.kuick.logging.LogLevel;
    return function ($receiver, tag, message) {
      if (tag === void 0)
        tag = null;
      var level = LogLevel.ERROR;
      if ($receiver.enabled_pg0dex$(level, tag))
        $receiver.log_do20u3$(level, message(), tag);
    };
  }));
  var fatal = defineInlineFunction('kuick-logging.kuick.logging.fatal_vkoxdw$', wrapFunction(function () {
    var LogLevel = _.kuick.logging.LogLevel;
    return function ($receiver, tag, message) {
      if (tag === void 0)
        tag = null;
      var level = LogLevel.ERROR;
      if ($receiver.enabled_pg0dex$(level, tag))
        $receiver.log_do20u3$(level, message(), tag);
    };
  }));
  function LogCreate$ObjectLiteral(closure$name) {
    this.name_8lzd9u$_0 = closure$name;
  }
  Object.defineProperty(LogCreate$ObjectLiteral.prototype, 'name', {
    get: function () {
      return this.name_8lzd9u$_0;
    }
  });
  LogCreate$ObjectLiteral.prototype.log_do20u3$ = function (level, message, tag) {
    switch (level.name) {
      case 'TRACE':
      case 'INFO':
        console.log(message, tag);
        break;
      case 'WARN':
        console.warn(message, tag);
        break;
      case 'ERROR':
      case 'FATAL':
        console.error(message, tag);
        break;
    }
  };
  LogCreate$ObjectLiteral.$metadata$ = {
    kind: Kind_CLASS,
    interfaces: [Logger_0]
  };
  function LogCreate(name) {
    return new LogCreate$ObjectLiteral(name);
  }
  function LogJs() {
    LogJs_instance = this;
  }
  Object.defineProperty(LogJs.prototype, 'loggers', {
    get: function () {
      var tmp$;
      var out = [];
      tmp$ = LoggerGetLoggers().iterator();
      while (tmp$.hasNext()) {
        var logger = tmp$.next();
        out.push(logger.name);
      }
      return out;
    }
  });
  LogJs.prototype.enableTag = function (name, tag) {
    get_config(Logger(name)).enableTag_61zpoe$(tag);
  };
  LogJs.prototype.disableTag = function (name, tag) {
    get_config(Logger(name)).disableTag_61zpoe$(tag);
  };
  LogJs.prototype.setMinLevel = function (name, level) {
    get_config(Logger(name)).minLevel = LogLevel$Companion_getInstance().invoke_61zpoe$(level);
  };
  LogJs.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'LogJs',
    interfaces: []
  };
  var LogJs_instance = null;
  function LogJs_getInstance() {
    if (LogJs_instance === null) {
      new LogJs();
    }
    return LogJs_instance;
  }
  function LogInit() {
    window.log = LogJs_getInstance();
  }
  var package$kuick = _.kuick || (_.kuick = {});
  var package$logging = package$kuick.logging || (package$kuick.logging = {});
  package$logging.Logger_61zpoe$ = Logger;
  package$logging.LoggerInit = LoggerInit;
  package$logging.LoggerGetLoggers = LoggerGetLoggers;
  LoggerConfig.prototype.ItemConfig = LoggerConfig$ItemConfig;
  Object.defineProperty(package$logging, 'LoggerConfig', {
    get: LoggerConfig_getInstance
  });
  Object.defineProperty(LogLevel, 'FATAL', {
    get: LogLevel$FATAL_getInstance
  });
  Object.defineProperty(LogLevel, 'ERROR', {
    get: LogLevel$ERROR_getInstance
  });
  Object.defineProperty(LogLevel, 'WARN', {
    get: LogLevel$WARN_getInstance
  });
  Object.defineProperty(LogLevel, 'INFO', {
    get: LogLevel$INFO_getInstance
  });
  Object.defineProperty(LogLevel, 'TRACE', {
    get: LogLevel$TRACE_getInstance
  });
  Object.defineProperty(LogLevel, 'Companion', {
    get: LogLevel$Companion_getInstance
  });
  package$logging.LogLevel = LogLevel;
  package$logging.Logger = Logger_0;
  package$logging.get_config_xnonvt$ = get_config;
  $$importsForInline$$['kuick-logging'] = _;
  package$logging.log_a2k8ig$ = log;
  package$logging.trace_vkoxdw$ = trace;
  package$logging.info_vkoxdw$ = info;
  package$logging.warn_vkoxdw$ = warn;
  package$logging.error_vkoxdw$ = error;
  package$logging.fatal_vkoxdw$ = fatal;
  package$logging.LogCreate_y4putb$ = LogCreate;
  Object.defineProperty(package$logging, 'LogJs', {
    get: LogJs_getInstance
  });
  package$logging.LogInit_8be2vx$ = LogInit;
  LogCreate$ObjectLiteral.prototype.enabled_pg0dex$$default = Logger_0.prototype.enabled_pg0dex$$default;
  LogCreate$ObjectLiteral.prototype.enabled_pg0dex$ = Logger_0.prototype.enabled_pg0dex$;
  loggers = LinkedHashMap_init();
  logInitialized = false;
  Kotlin.defineModule('kuick-logging', _);
  return _;
}));

//# sourceMappingURL=kuick-logging.js.map
