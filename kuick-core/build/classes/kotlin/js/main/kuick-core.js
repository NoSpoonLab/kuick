(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'kuick-core'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'kuick-core'.");
    }
    root['kuick-core'] = factory(typeof this['kuick-core'] === 'undefined' ? {} : this['kuick-core'], kotlin);
  }
}(this, function (_, Kotlin) {
  'use strict';
  var $$importsForInline$$ = _.$$importsForInline$$ || (_.$$importsForInline$$ = {});
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var Annotation = Kotlin.kotlin.Annotation;
  var equals = Kotlin.equals;
  var joinToString = Kotlin.kotlin.collections.joinToString_fmv235$;
  var Kind_INTERFACE = Kotlin.Kind.INTERFACE;
  var COROUTINE_SUSPENDED = Kotlin.kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED;
  var CoroutineImpl = Kotlin.kotlin.coroutines.CoroutineImpl;
  var SuspendFunction1 = Function;
  var throwCCE = Kotlin.throwCCE;
  var Unit = Kotlin.kotlin.Unit;
  var println = Kotlin.kotlin.io.println_s8jyv4$;
  var MutableMap = Kotlin.kotlin.collections.MutableMap;
  var substringBeforeLast = Kotlin.kotlin.text.substringBeforeLast_8cymmc$;
  var split = Kotlin.kotlin.text.split_ip8yn$;
  var toInt = Kotlin.kotlin.text.toInt_pdl1vz$;
  var padStart = Kotlin.kotlin.text.padStart_vrc1nu$;
  var Comparable = Kotlin.kotlin.Comparable;
  var round = Kotlin.kotlin.math.round_14dthe$;
  var toString = Kotlin.toString;
  var roundToInt = Kotlin.kotlin.math.roundToInt_yrwdxr$;
  var startsWith = Kotlin.kotlin.text.startsWith_7epoxm$;
  var listOf = Kotlin.kotlin.collections.listOf_mh5how$;
  var plus = Kotlin.kotlin.collections.plus_mydzjv$;
  var defineInlineFunction = Kotlin.defineInlineFunction;
  var wrapFunction = Kotlin.wrapFunction;
  var firstOrNull = Kotlin.kotlin.collections.firstOrNull_2p1efm$;
  var toList = Kotlin.kotlin.collections.toList_7wnvza$;
  var NotImplementedError = Kotlin.kotlin.NotImplementedError;
  var contains = Kotlin.kotlin.text.contains_li3zpu$;
  var contains_0 = Kotlin.kotlin.collections.contains_2ws7j4$;
  var IllegalArgumentException_init = Kotlin.kotlin.IllegalArgumentException_init;
  var Enum = Kotlin.kotlin.Enum;
  var throwISE = Kotlin.throwISE;
  var emptyList = Kotlin.kotlin.collections.emptyList_287e2$;
  var plus_0 = Kotlin.kotlin.collections.plus_qloxvw$;
  ModelFilterExp.prototype = Object.create(ModelQuery.prototype);
  ModelFilterExp.prototype.constructor = ModelFilterExp;
  FilterExpNot.prototype = Object.create(ModelFilterExp.prototype);
  FilterExpNot.prototype.constructor = FilterExpNot;
  FilterExpAnd.prototype = Object.create(ModelFilterExp.prototype);
  FilterExpAnd.prototype.constructor = FilterExpAnd;
  FilterExpOr.prototype = Object.create(ModelFilterExp.prototype);
  FilterExpOr.prototype.constructor = FilterExpOr;
  FieldUnop.prototype = Object.create(ModelFilterExp.prototype);
  FieldUnop.prototype.constructor = FieldUnop;
  FieldIsNull.prototype = Object.create(FieldUnop.prototype);
  FieldIsNull.prototype.constructor = FieldIsNull;
  FieldBinop.prototype = Object.create(ModelFilterExp.prototype);
  FieldBinop.prototype.constructor = FieldBinop;
  SimpleFieldBinop.prototype = Object.create(FieldBinop.prototype);
  SimpleFieldBinop.prototype.constructor = SimpleFieldBinop;
  FieldEqs.prototype = Object.create(SimpleFieldBinop.prototype);
  FieldEqs.prototype.constructor = FieldEqs;
  FieldLike.prototype = Object.create(SimpleFieldBinop.prototype);
  FieldLike.prototype.constructor = FieldLike;
  FieldGt.prototype = Object.create(SimpleFieldBinop.prototype);
  FieldGt.prototype.constructor = FieldGt;
  FieldGte.prototype = Object.create(SimpleFieldBinop.prototype);
  FieldGte.prototype.constructor = FieldGte;
  FieldLt.prototype = Object.create(SimpleFieldBinop.prototype);
  FieldLt.prototype.constructor = FieldLt;
  FieldLte.prototype = Object.create(SimpleFieldBinop.prototype);
  FieldLte.prototype.constructor = FieldLte;
  FieldBinopOnSet.prototype = Object.create(FieldBinop.prototype);
  FieldBinopOnSet.prototype.constructor = FieldBinopOnSet;
  FieldWithin.prototype = Object.create(FieldBinopOnSet.prototype);
  FieldWithin.prototype.constructor = FieldWithin;
  FieldWithinComplex.prototype = Object.create(FieldBinopOnSet.prototype);
  FieldWithinComplex.prototype.constructor = FieldWithinComplex;
  CachedModelRepository.prototype = Object.create(ModelRepositoryDecorator.prototype);
  CachedModelRepository.prototype.constructor = CachedModelRepository;
  FastEventuallyConsistentModelRepositoryDecorator.prototype = Object.create(BusModelRepositoryDecorator.prototype);
  FastEventuallyConsistentModelRepositoryDecorator.prototype.constructor = FastEventuallyConsistentModelRepositoryDecorator;
  ModelChangeType.prototype = Object.create(Enum.prototype);
  ModelChangeType.prototype.constructor = ModelChangeType;
  ViewRepositoryDecorator.prototype = Object.create(AbstractViewRepositoryDecorator.prototype);
  ViewRepositoryDecorator.prototype.constructor = ViewRepositoryDecorator;
  function Exact() {
  }
  Exact.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Exact',
    interfaces: [Annotation]
  };
  function WithCache(name, on) {
    this.name = name;
    this.on = on;
  }
  WithCache.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'WithCache',
    interfaces: [Annotation]
  };
  function toInfo($receiver) {
    return new CacheInfo($receiver.name, $receiver.on);
  }
  function Cached(name, on) {
    if (name === void 0)
      name = '';
    if (on === void 0) {
      on = [];
    }
    this.name = name;
    this.on = on;
  }
  Cached.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Cached',
    interfaces: [Annotation]
  };
  function CacheInfo(name, on) {
    this.name = name;
    this.on = on;
  }
  CacheInfo.prototype.withDefault_fekwrl$ = function (def) {
    return this.copy_vlmfdj$(equals(this.name, '') ? def.name : this.name, this.on.length === 0 ? def.on : this.on);
  };
  var ArrayList_init = Kotlin.kotlin.collections.ArrayList_init_ww73n8$;
  CacheInfo.prototype.cacheKey = function () {
    var $receiver = this.on;
    var destination = ArrayList_init($receiver.length);
    var tmp$;
    for (tmp$ = 0; tmp$ !== $receiver.length; ++tmp$) {
      var item = $receiver[tmp$];
      destination.add_11rb$(String.fromCharCode(36) + '{' + item + '}');
    }
    return '"' + joinToString(destination, '|') + '"';
  };
  CacheInfo.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CacheInfo',
    interfaces: []
  };
  CacheInfo.prototype.component1 = function () {
    return this.name;
  };
  CacheInfo.prototype.component2 = function () {
    return this.on;
  };
  CacheInfo.prototype.copy_vlmfdj$ = function (name, on) {
    return new CacheInfo(name === void 0 ? this.name : name, on === void 0 ? this.on : on);
  };
  CacheInfo.prototype.toString = function () {
    return 'CacheInfo(name=' + Kotlin.toString(this.name) + (', on=' + Kotlin.toString(this.on)) + ')';
  };
  CacheInfo.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.name) | 0;
    result = result * 31 + Kotlin.hashCode(this.on) | 0;
    return result;
  };
  CacheInfo.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.name, other.name) && Kotlin.equals(this.on, other.on)))));
  };
  function toInfo_0($receiver) {
    return new CacheInfo($receiver.name, $receiver.on);
  }
  function InvalidatesCache(name, on) {
    if (name === void 0)
      name = '';
    if (on === void 0)
      on = '';
    this.name = name;
    this.on = on;
  }
  InvalidatesCache.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'InvalidatesCache',
    interfaces: [Annotation]
  };
  function Bus() {
  }
  Bus.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'Bus',
    interfaces: []
  };
  var LinkedHashMap_init = Kotlin.kotlin.collections.LinkedHashMap_init_q3lmfv$;
  function SyncBus() {
    this.listenersMap_0 = LinkedHashMap_init();
  }
  function Coroutine$publishAsync_fym6v3$($this, topicName_0, event_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$tmp$ = void 0;
    this.local$topicName = topicName_0;
    this.local$event = event_0;
  }
  Coroutine$publishAsync_fym6v3$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$publishAsync_fym6v3$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$publishAsync_fym6v3$.prototype.constructor = Coroutine$publishAsync_fym6v3$;
  Coroutine$publishAsync_fym6v3$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.local$tmp$ = this.$this.eventListeners_0(this.local$topicName).iterator();
            this.state_0 = 2;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 4;
              continue;
            }

            var element = this.local$tmp$.next();
            var tmp$;
            this.state_0 = 3;
            this.result_0 = (Kotlin.isType(tmp$ = element, SuspendFunction1) ? tmp$ : throwCCE())(this.local$event, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            this.state_0 = 2;
            continue;
          case 4:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  SyncBus.prototype.publishAsync_fym6v3$ = function (topicName_0, event_0, continuation_0, suspended) {
    var instance = new Coroutine$publishAsync_fym6v3$(this, topicName_0, event_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  SyncBus.prototype.registerAsync_fcawk$ = function (topicName, listener) {
    this.eventListeners_0(topicName).add_11rb$(listener);
  };
  var ArrayList_init_0 = Kotlin.kotlin.collections.ArrayList_init_287e2$;
  SyncBus.prototype.eventListeners_0 = function (topic) {
    var $receiver = this.listenersMap_0;
    var tmp$;
    var value = $receiver.get_11rb$(topic);
    if (value == null) {
      var answer = ArrayList_init_0();
      $receiver.put_xwzc9p$(topic, answer);
      tmp$ = answer;
    }
     else {
      tmp$ = value;
    }
    return tmp$;
  };
  SyncBus.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'SyncBus',
    interfaces: [Bus]
  };
  var LinkedHashSet_init = Kotlin.kotlin.collections.LinkedHashSet_init_287e2$;
  function CacheManager(bus) {
    this.bus = bus;
    this.caches_0 = LinkedHashMap_init();
    this.listening_0 = LinkedHashSet_init();
  }
  function Coroutine$CacheManager$cached$lambda$lambda$lambda(closure$cacheName_0, closure$key_0, closure$cachesOn_0, this$CacheManager_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$closure$cacheName = closure$cacheName_0;
    this.local$closure$key = closure$key_0;
    this.local$closure$cachesOn = closure$cachesOn_0;
    this.local$this$CacheManager = this$CacheManager_0;
    this.local$it = it_0;
  }
  Coroutine$CacheManager$cached$lambda$lambda$lambda.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$CacheManager$cached$lambda$lambda$lambda.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$CacheManager$cached$lambda$lambda$lambda.prototype.constructor = Coroutine$CacheManager$cached$lambda$lambda$lambda;
  Coroutine$CacheManager$cached$lambda$lambda$lambda.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            println('Invalidating derivated cache: ' + this.local$closure$cacheName + '/' + this.local$closure$key + ' based on ' + this.local$closure$cachesOn + '/' + this.local$closure$key);
            this.local$this$CacheManager.cacheByName_0(this.local$closure$cacheName).remove_11rb$(this.local$it);
            this.state_0 = 2;
            this.result_0 = this.local$this$CacheManager.bus.publishAsync_fym6v3$(this.local$closure$cacheName, this.local$closure$key, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function CacheManager$cached$lambda$lambda$lambda(closure$cacheName_0, closure$key_0, closure$cachesOn_0, this$CacheManager_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$CacheManager$cached$lambda$lambda$lambda(closure$cacheName_0, closure$key_0, closure$cachesOn_0, this$CacheManager_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$CacheManager$cached$lambda(closure$cachesOn_0, this$CacheManager_0, closure$cacheName_0, closure$key_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$closure$cachesOn = closure$cachesOn_0;
    this.local$this$CacheManager = this$CacheManager_0;
    this.local$closure$cacheName = closure$cacheName_0;
    this.local$closure$key = closure$key_0;
  }
  Coroutine$CacheManager$cached$lambda.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$CacheManager$cached$lambda.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$CacheManager$cached$lambda.prototype.constructor = Coroutine$CacheManager$cached$lambda;
  Coroutine$CacheManager$cached$lambda.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            var $receiver = this.local$closure$cachesOn;
            var tmp$;
            for (tmp$ = 0; tmp$ !== $receiver.length; ++tmp$) {
              var element = $receiver[tmp$];
              var this$CacheManager = this.local$this$CacheManager;
              this$CacheManager.bus.registerAsync_fcawk$(element, CacheManager$cached$lambda$lambda$lambda(this.local$closure$cacheName, this.local$closure$key, element, this$CacheManager));
            }

            return Unit;
          case 1:
            throw this.exception_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function CacheManager$cached$lambda(closure$cachesOn_0, this$CacheManager_0, closure$cacheName_0, closure$key_0) {
    return function (continuation_0, suspended) {
      var instance = new Coroutine$CacheManager$cached$lambda(closure$cachesOn_0, this$CacheManager_0, closure$cacheName_0, closure$key_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$CacheManager$cached$lambda_0(closure$cacheName_0, closure$key_0, closure$cachesOn_0, closure$compute_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$closure$cacheName = closure$cacheName_0;
    this.local$closure$key = closure$key_0;
    this.local$closure$cachesOn = closure$cachesOn_0;
    this.local$closure$compute = closure$compute_0;
  }
  Coroutine$CacheManager$cached$lambda_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$CacheManager$cached$lambda_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$CacheManager$cached$lambda_0.prototype.constructor = Coroutine$CacheManager$cached$lambda_0;
  Coroutine$CacheManager$cached$lambda_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            println('Caching: ' + this.local$closure$cacheName + '/' + this.local$closure$key + ' based on ' + this.local$closure$cachesOn + '/' + this.local$closure$key);
            this.state_0 = 2;
            this.result_0 = this.local$closure$compute(this.local$closure$key, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function CacheManager$cached$lambda_0(closure$cacheName_0, closure$key_0, closure$cachesOn_0, closure$compute_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$CacheManager$cached$lambda_0(closure$cacheName_0, closure$key_0, closure$cachesOn_0, closure$compute_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$cached_wjsju5$($this, cacheName_0, key_0, compute_0, cachesOn_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$cacheName = cacheName_0;
    this.local$key = key_0;
    this.local$compute = compute_0;
    this.local$cachesOn = cachesOn_0;
  }
  Coroutine$cached_wjsju5$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$cached_wjsju5$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$cached_wjsju5$.prototype.constructor = Coroutine$cached_wjsju5$;
  Coroutine$cached_wjsju5$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.once_0(this.local$cacheName, CacheManager$cached$lambda(this.local$cachesOn, this.$this, this.local$cacheName, this.local$key), this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = getOrPutSuspendable(this.$this.cacheByName_0(this.local$cacheName), this.local$key, CacheManager$cached$lambda_0(this.local$cacheName, this.local$key, this.local$cachesOn, this.local$compute), this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CacheManager.prototype.cached_wjsju5$ = function (cacheName_0, key_0, compute_0, cachesOn_0, continuation_0, suspended) {
    var instance = new Coroutine$cached_wjsju5$(this, cacheName_0, key_0, compute_0, cachesOn_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$invalidates_hy1r64$($this, cacheName_0, key_0, action_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$out = void 0;
    this.local$cacheName = cacheName_0;
    this.local$key = key_0;
    this.local$action = action_0;
  }
  Coroutine$invalidates_hy1r64$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$invalidates_hy1r64$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$invalidates_hy1r64$.prototype.constructor = Coroutine$invalidates_hy1r64$;
  Coroutine$invalidates_hy1r64$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$action(this.local$key, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$out = this.result_0;
            println('Invalidating cache: ' + this.local$cacheName + '/' + this.local$key);
            this.state_0 = 3;
            this.result_0 = this.$this.bus.publishAsync_fym6v3$(this.local$cacheName, this.local$key, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.local$out;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CacheManager.prototype.invalidates_hy1r64$ = function (cacheName_0, key_0, action_0, continuation_0, suspended) {
    var instance = new Coroutine$invalidates_hy1r64$(this, cacheName_0, key_0, action_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  CacheManager.prototype.cacheByName_0 = function (name) {
    var tmp$;
    var $receiver = this.caches_0;
    var tmp$_0;
    var value = $receiver.get_11rb$(name);
    if (value == null) {
      var answer = LinkedHashMap_init();
      $receiver.put_xwzc9p$(name, answer);
      tmp$_0 = answer;
    }
     else {
      tmp$_0 = value;
    }
    return Kotlin.isType(tmp$ = tmp$_0, MutableMap) ? tmp$ : throwCCE();
  };
  function Coroutine$once_0($this, name_0, action_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$name = name_0;
    this.local$action = action_0;
  }
  Coroutine$once_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$once_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$once_0.prototype.constructor = Coroutine$once_0;
  Coroutine$once_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            if (!this.$this.listening_0.contains_11rb$(this.local$name)) {
              this.$this.listening_0.add_11rb$(this.local$name);
              this.state_0 = 2;
              this.result_0 = this.local$action(this);
              if (this.result_0 === COROUTINE_SUSPENDED)
                return COROUTINE_SUSPENDED;
              continue;
            }
             else {
              this.state_0 = 3;
              continue;
            }

          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            continue;
          case 3:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CacheManager.prototype.once_0 = function (name_0, action_0, continuation_0, suspended) {
    var instance = new Coroutine$once_0(this, name_0, action_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  CacheManager.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CacheManager',
    interfaces: []
  };
  function Coroutine$getOrPutSuspendable($receiver_0, key_0, calc_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$out = void 0;
    this.local$$receiver = $receiver_0;
    this.local$key = key_0;
    this.local$calc = calc_0;
  }
  Coroutine$getOrPutSuspendable.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$getOrPutSuspendable.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$getOrPutSuspendable.prototype.constructor = Coroutine$getOrPutSuspendable;
  Coroutine$getOrPutSuspendable.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.local$out = this.local$$receiver.get_11rb$(this.local$key);
            if (this.local$out == null) {
              this.state_0 = 2;
              this.result_0 = this.local$calc(this.local$key, this);
              if (this.result_0 === COROUTINE_SUSPENDED)
                return COROUTINE_SUSPENDED;
              continue;
            }
             else {
              this.state_0 = 3;
              continue;
            }

          case 1:
            throw this.exception_0;
          case 2:
            this.local$out = this.result_0;
            this.local$$receiver.put_xwzc9p$(this.local$key, this.local$out);
            this.state_0 = 3;
            continue;
          case 3:
            return this.local$out;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function getOrPutSuspendable($receiver_0, key_0, calc_0, continuation_0, suspended) {
    var instance = new Coroutine$getOrPutSuspendable($receiver_0, key_0, calc_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  }
  function CachedRepository() {
  }
  CachedRepository.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CachedRepository',
    interfaces: []
  };
  function Email(email) {
    this.email = email;
  }
  var trim = Kotlin.kotlin.text.trim_gw00vp$;
  Email.prototype.normalized = function () {
    var $receiver = this.email.toLowerCase();
    var tmp$;
    return trim(Kotlin.isCharSequence(tmp$ = $receiver) ? tmp$ : throwCCE()).toString();
  };
  Email.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Email',
    interfaces: []
  };
  Email.prototype.component1 = function () {
    return this.email;
  };
  Email.prototype.copy_61zpoe$ = function (email) {
    return new Email(email === void 0 ? this.email : email);
  };
  Email.prototype.toString = function () {
    return 'Email(email=' + Kotlin.toString(this.email) + ')';
  };
  Email.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.email) | 0;
    return result;
  };
  Email.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && Kotlin.equals(this.email, other.email))));
  };
  function Id() {
  }
  Id.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'Id',
    interfaces: []
  };
  function IdProvider() {
  }
  IdProvider.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'IdProvider',
    interfaces: []
  };
  function parentPath($receiver) {
    return equals($receiver, '') ? null : substringBeforeLast($receiver, 47);
  }
  var collectionSizeOrDefault = Kotlin.kotlin.collections.collectionSizeOrDefault_ba2ldo$;
  function pathToSectionNumber(path) {
    var $receiver = split(path, ['/']);
    var destination = ArrayList_init_0();
    var tmp$;
    tmp$ = $receiver.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      if (!equals(element, ''))
        destination.add_11rb$(element);
    }
    var destination_0 = ArrayList_init(collectionSizeOrDefault(destination, 10));
    var tmp$_0;
    tmp$_0 = destination.iterator();
    while (tmp$_0.hasNext()) {
      var item = tmp$_0.next();
      destination_0.add_11rb$(toInt(item));
    }
    return joinToString(destination_0, '.');
  }
  function sectionNumberToPath(sectionNumber) {
    if (equals(sectionNumber, ''))
      return '';
    else {
      var $receiver = split(sectionNumber, ['.']);
      var destination = ArrayList_init(collectionSizeOrDefault($receiver, 10));
      var tmp$;
      tmp$ = $receiver.iterator();
      while (tmp$.hasNext()) {
        var item = tmp$.next();
        destination.add_11rb$(padStart(item, 2, 48));
      }
      return joinToString(destination, '/', '/');
    }
  }
  var K_LOCAL_DATE_FORMAT;
  var K_LOCAL_DATE_TIME_FORMAT;
  function KLocalDate(date) {
    this.date = date;
  }
  KLocalDate.prototype.compareTo_11rb$ = function (other) {
    return Kotlin.compareTo(this.date, other.date);
  };
  KLocalDate.prototype.toString = function () {
    return this.date;
  };
  KLocalDate.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'KLocalDate',
    interfaces: [Comparable]
  };
  KLocalDate.prototype.component1 = function () {
    return this.date;
  };
  KLocalDate.prototype.copy_61zpoe$ = function (date) {
    return new KLocalDate(date === void 0 ? this.date : date);
  };
  KLocalDate.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.date) | 0;
    return result;
  };
  KLocalDate.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && Kotlin.equals(this.date, other.date))));
  };
  function KDateRange(from, to) {
    this.from = from;
    this.to = to;
  }
  KDateRange.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'KDateRange',
    interfaces: []
  };
  KDateRange.prototype.component1 = function () {
    return this.from;
  };
  KDateRange.prototype.component2 = function () {
    return this.to;
  };
  KDateRange.prototype.copy_5tpu52$ = function (from, to) {
    return new KDateRange(from === void 0 ? this.from : from, to === void 0 ? this.to : to);
  };
  KDateRange.prototype.toString = function () {
    return 'KDateRange(from=' + Kotlin.toString(this.from) + (', to=' + Kotlin.toString(this.to)) + ')';
  };
  KDateRange.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.from) | 0;
    result = result * 31 + Kotlin.hashCode(this.to) | 0;
    return result;
  };
  KDateRange.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.from, other.from) && Kotlin.equals(this.to, other.to)))));
  };
  function normalized($receiver) {
    return ($receiver != null ? $receiver.date : null) == null ? null : $receiver;
  }
  function TimeService() {
  }
  TimeService.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'TimeService',
    interfaces: []
  };
  function daysBetween($receiver, dateRange) {
    return $receiver.daysBetween_5tpu52$(dateRange.from, dateRange.to);
  }
  function secondsToHuman$measure(amount, unit) {
    return amount === 0.0 ? '' : '' + toString(round(amount)) + ' ' + unit;
  }
  var Math_0 = Math;
  function secondsToHuman$measureFloor(amount, unit) {
    return amount === 0.0 ? '' : '' + toString(Math_0.floor(amount)) + ' ' + unit;
  }
  function secondsToHuman(_input) {
    var input = _input;
    var measure = secondsToHuman$measure;
    var measureFloor = secondsToHuman$measureFloor;
    if (input == null)
      return '-';
    if (input === 0.0)
      return '0 seg';
    var lapse = '';
    if (input < 60)
      lapse = measure(input, 'seg');
    else if (input >= 60 && input < 3600)
      lapse = measure(input / 60, 'min');
    else
      lapse = measureFloor(input / 3600, 'h') + ' ' + measureFloor(input % 3600 / 60, 'min');
    return lapse;
  }
  function toPercent($receiver) {
    return roundToInt($receiver * 100).toString() + '%';
  }
  function NumberedTree() {
  }
  NumberedTree.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'NumberedTree',
    interfaces: []
  };
  function findByNumber($receiver, _number, node) {
    if (node === void 0)
      node = $receiver.rootNode();
    if (equals(_number, $receiver.nodeNumber_l5gk0$(node)))
      return node;
    else if (equals(node, $receiver.rootNode()) || startsWith(_number, $receiver.nodeNumber_l5gk0$(node) + '.')) {
      var $receiver_0 = $receiver.nodeChildren_l5gk0$(node);
      var firstOrNull$result;
      firstOrNull$break: do {
        var tmp$;
        for (tmp$ = 0; tmp$ !== $receiver_0.length; ++tmp$) {
          var element = $receiver_0[tmp$];
          if (findByNumber($receiver, _number, element) != null) {
            firstOrNull$result = element;
            break firstOrNull$break;
          }
        }
        firstOrNull$result = null;
      }
       while (false);
      var subnode = firstOrNull$result;
      return subnode == null ? null : findByNumber($receiver, _number, subnode);
    }
     else
      return null;
  }
  function parentNumber($receiver) {
    return substringBeforeLast($receiver, 46);
  }
  function isRoot($receiver, node) {
    return equals($receiver.nodeNumber_l5gk0$(node), '');
  }
  function hasChildren($receiver, node) {
    return !($receiver.nodeChildren_l5gk0$(node).length === 0);
  }
  function flat($receiver) {
    return flat_0($receiver, $receiver.rootNode());
  }
  var addAll = Kotlin.kotlin.collections.addAll_ipc267$;
  function flat_0($receiver, node) {
    var tmp$ = listOf(node);
    var $receiver_0 = $receiver.nodeChildren_l5gk0$(node);
    var destination = ArrayList_init_0();
    var tmp$_0;
    for (tmp$_0 = 0; tmp$_0 !== $receiver_0.length; ++tmp$_0) {
      var element = $receiver_0[tmp$_0];
      var list = flat_0($receiver, element);
      addAll(destination, list);
    }
    return plus(tmp$, destination);
  }
  function findNextByNumber($receiver, _number) {
    return findByNumberAndOffset($receiver, _number, 1);
  }
  function findPreviousByNumber($receiver, _number) {
    return findByNumberAndOffset($receiver, _number, -1);
  }
  function findByNumberAndOffset($receiver, _number, offset) {
    var flat_0 = flat($receiver);
    var indexOfFirst$result;
    indexOfFirst$break: do {
      var tmp$;
      var index = 0;
      tmp$ = flat_0.iterator();
      while (tmp$.hasNext()) {
        var item = tmp$.next();
        if (equals($receiver.nodeNumber_l5gk0$(item), _number)) {
          indexOfFirst$result = index;
          break indexOfFirst$break;
        }
        index = index + 1 | 0;
      }
      indexOfFirst$result = -1;
    }
     while (false);
    var currentIdx = indexOfFirst$result;
    var next = (flat_0.size + currentIdx + offset | 0) % flat_0.size;
    return flat_0.get_za3lpa$(next);
  }
  function ModelQuery() {
  }
  ModelQuery.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ModelQuery',
    interfaces: []
  };
  function ModelFilterExp() {
    ModelQuery.call(this);
  }
  ModelFilterExp.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ModelFilterExp',
    interfaces: [ModelQuery]
  };
  function FilterExpNot(exp) {
    ModelFilterExp.call(this);
    this.exp = exp;
  }
  FilterExpNot.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FilterExpNot',
    interfaces: [ModelFilterExp]
  };
  FilterExpNot.prototype.component1 = function () {
    return this.exp;
  };
  FilterExpNot.prototype.copy_yp8rvk$ = function (exp) {
    return new FilterExpNot(exp === void 0 ? this.exp : exp);
  };
  FilterExpNot.prototype.toString = function () {
    return 'FilterExpNot(exp=' + Kotlin.toString(this.exp) + ')';
  };
  FilterExpNot.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.exp) | 0;
    return result;
  };
  FilterExpNot.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && Kotlin.equals(this.exp, other.exp))));
  };
  function not(exp) {
    return new FilterExpNot(exp);
  }
  function FilterExpAnd(left, right) {
    ModelFilterExp.call(this);
    this.left = left;
    this.right = right;
  }
  FilterExpAnd.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FilterExpAnd',
    interfaces: [ModelFilterExp]
  };
  FilterExpAnd.prototype.component1 = function () {
    return this.left;
  };
  FilterExpAnd.prototype.component2 = function () {
    return this.right;
  };
  FilterExpAnd.prototype.copy_3wl8ek$ = function (left, right) {
    return new FilterExpAnd(left === void 0 ? this.left : left, right === void 0 ? this.right : right);
  };
  FilterExpAnd.prototype.toString = function () {
    return 'FilterExpAnd(left=' + Kotlin.toString(this.left) + (', right=' + Kotlin.toString(this.right)) + ')';
  };
  FilterExpAnd.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.left) | 0;
    result = result * 31 + Kotlin.hashCode(this.right) | 0;
    return result;
  };
  FilterExpAnd.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.left, other.left) && Kotlin.equals(this.right, other.right)))));
  };
  function and($receiver, right) {
    return new FilterExpAnd($receiver, right);
  }
  function FilterExpOr(left, right) {
    ModelFilterExp.call(this);
    this.left = left;
    this.right = right;
  }
  FilterExpOr.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FilterExpOr',
    interfaces: [ModelFilterExp]
  };
  FilterExpOr.prototype.component1 = function () {
    return this.left;
  };
  FilterExpOr.prototype.component2 = function () {
    return this.right;
  };
  FilterExpOr.prototype.copy_3wl8ek$ = function (left, right) {
    return new FilterExpOr(left === void 0 ? this.left : left, right === void 0 ? this.right : right);
  };
  FilterExpOr.prototype.toString = function () {
    return 'FilterExpOr(left=' + Kotlin.toString(this.left) + (', right=' + Kotlin.toString(this.right)) + ')';
  };
  FilterExpOr.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.left) | 0;
    result = result * 31 + Kotlin.hashCode(this.right) | 0;
    return result;
  };
  FilterExpOr.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.left, other.left) && Kotlin.equals(this.right, other.right)))));
  };
  function or($receiver, right) {
    return new FilterExpOr($receiver, right);
  }
  function FieldUnop(field) {
    ModelFilterExp.call(this);
    this.field = field;
  }
  FieldUnop.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldUnop',
    interfaces: [ModelFilterExp]
  };
  function FieldIsNull(field) {
    FieldUnop.call(this, field);
  }
  FieldIsNull.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldIsNull',
    interfaces: [FieldUnop]
  };
  function isNull($receiver) {
    return new FieldIsNull($receiver);
  }
  function FieldBinop(field, value) {
    ModelFilterExp.call(this);
    this.field = field;
    this.value = value;
  }
  FieldBinop.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldBinop',
    interfaces: [ModelFilterExp]
  };
  function SimpleFieldBinop(field, value) {
    FieldBinop.call(this, field, value);
  }
  SimpleFieldBinop.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'SimpleFieldBinop',
    interfaces: [FieldBinop]
  };
  function FieldEqs(field, value) {
    SimpleFieldBinop.call(this, field, value);
  }
  FieldEqs.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldEqs',
    interfaces: [SimpleFieldBinop]
  };
  function eq($receiver, value) {
    return new FieldEqs($receiver, value);
  }
  function FieldLike(field, value) {
    SimpleFieldBinop.call(this, field, value);
  }
  FieldLike.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldLike',
    interfaces: [SimpleFieldBinop]
  };
  function like($receiver, value) {
    return new FieldLike($receiver, value);
  }
  function FieldGt(field, value) {
    SimpleFieldBinop.call(this, field, value);
  }
  FieldGt.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldGt',
    interfaces: [SimpleFieldBinop]
  };
  function gt($receiver, value) {
    return new FieldGt($receiver, value);
  }
  function FieldGte(field, value) {
    SimpleFieldBinop.call(this, field, value);
  }
  FieldGte.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldGte',
    interfaces: [SimpleFieldBinop]
  };
  function gte($receiver, value) {
    return new FieldGte($receiver, value);
  }
  function FieldLt(field, value) {
    SimpleFieldBinop.call(this, field, value);
  }
  FieldLt.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldLt',
    interfaces: [SimpleFieldBinop]
  };
  function lt($receiver, value) {
    return new FieldLt($receiver, value);
  }
  function FieldLte(field, value) {
    SimpleFieldBinop.call(this, field, value);
  }
  FieldLte.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldLte',
    interfaces: [SimpleFieldBinop]
  };
  function lte($receiver, value) {
    return new FieldLte($receiver, value);
  }
  function FieldBinopOnSet(field, value) {
    FieldBinop.call(this, field, value);
  }
  FieldBinopOnSet.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldBinopOnSet',
    interfaces: [FieldBinop]
  };
  function FieldWithin(field, value) {
    FieldBinopOnSet.call(this, field, value);
  }
  FieldWithin.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldWithin',
    interfaces: [FieldBinopOnSet]
  };
  function FieldWithinComplex(field, value) {
    FieldBinopOnSet.call(this, field, value);
  }
  FieldWithinComplex.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FieldWithinComplex',
    interfaces: [FieldBinopOnSet]
  };
  var within = defineInlineFunction('kuick-core.kuick.repositories.within_rz75nj$', wrapFunction(function () {
    var FieldWithin_init = _.kuick.repositories.FieldWithin;
    var FieldWithinComplex_init = _.kuick.repositories.FieldWithinComplex;
    var PrimitiveClasses$booleanClass = Kotlin.kotlin.reflect.js.internal.PrimitiveClasses.booleanClass;
    var PrimitiveClasses$numberClass = Kotlin.kotlin.reflect.js.internal.PrimitiveClasses.numberClass;
    var PrimitiveClasses$stringClass = Kotlin.kotlin.reflect.js.internal.PrimitiveClasses.stringClass;
    var getKClass = Kotlin.getKClass;
    var Char = Kotlin.BoxedChar;
    var setOf = Kotlin.kotlin.collections.setOf_i5x0yv$;
    var Collection = Kotlin.kotlin.collections.Collection;
    return function (V_0, isV, $receiver, value) {
      var $receiver_0 = setOf([PrimitiveClasses$booleanClass, PrimitiveClasses$numberClass, PrimitiveClasses$stringClass, getKClass(Char)]);
      var any$result;
      any$break: do {
        var tmp$;
        if (Kotlin.isType($receiver_0, Collection) && $receiver_0.isEmpty()) {
          any$result = false;
          break any$break;
        }
        tmp$ = $receiver_0.iterator();
        while (tmp$.hasNext()) {
          var element = tmp$.next();
          if (element != null ? element.equals(getKClass(V_0)) : null) {
            any$result = true;
            break any$break;
          }
        }
        any$result = false;
      }
       while (false);
      return any$result ? new FieldWithin_init($receiver, value) : new FieldWithinComplex_init($receiver, value);
    };
  }));
  var isBasicType = defineInlineFunction('kuick-core.kuick.repositories.isBasicType_30y1fr$', wrapFunction(function () {
    var PrimitiveClasses$booleanClass = Kotlin.kotlin.reflect.js.internal.PrimitiveClasses.booleanClass;
    var PrimitiveClasses$numberClass = Kotlin.kotlin.reflect.js.internal.PrimitiveClasses.numberClass;
    var PrimitiveClasses$stringClass = Kotlin.kotlin.reflect.js.internal.PrimitiveClasses.stringClass;
    var getKClass = Kotlin.getKClass;
    var Char = Kotlin.BoxedChar;
    var setOf = Kotlin.kotlin.collections.setOf_i5x0yv$;
    var Collection = Kotlin.kotlin.collections.Collection;
    return function (V_0, isV) {
      var $receiver = setOf([PrimitiveClasses$booleanClass, PrimitiveClasses$numberClass, PrimitiveClasses$stringClass, getKClass(Char)]);
      var any$result;
      any$break: do {
        var tmp$;
        if (Kotlin.isType($receiver, Collection) && $receiver.isEmpty()) {
          any$result = false;
          break any$break;
        }
        tmp$ = $receiver.iterator();
        while (tmp$.hasNext()) {
          var element = tmp$.next();
          if (element != null ? element.equals(getKClass(V_0)) : null) {
            any$result = true;
            break any$break;
          }
        }
        any$result = false;
      }
       while (false);
      return any$result;
    };
  }));
  function ModelRepository() {
  }
  function Coroutine$insertMany_943sby$($this, collection_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$forEach$result = void 0;
    this.local$tmp$ = void 0;
    this.local$collection = collection_0;
  }
  Coroutine$insertMany_943sby$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$insertMany_943sby$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$insertMany_943sby$.prototype.constructor = Coroutine$insertMany_943sby$;
  Coroutine$insertMany_943sby$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.local$tmp$ = this.local$collection.iterator();
            this.state_0 = 2;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 4;
              continue;
            }

            var element = this.local$tmp$.next();
            this.state_0 = 3;
            this.result_0 = this.$this.insert_nqi3in$(element, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            this.state_0 = 2;
            continue;
          case 4:
            return this.local$forEach$result;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepository.prototype.insertMany_943sby$ = function (collection_0, continuation_0, suspended) {
    var instance = new Coroutine$insertMany_943sby$(this, collection_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$updateMany_943sby$($this, collection_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$forEach$result = void 0;
    this.local$tmp$ = void 0;
    this.local$collection = collection_0;
  }
  Coroutine$updateMany_943sby$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$updateMany_943sby$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$updateMany_943sby$.prototype.constructor = Coroutine$updateMany_943sby$;
  Coroutine$updateMany_943sby$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.local$tmp$ = this.local$collection.iterator();
            this.state_0 = 2;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 4;
              continue;
            }

            var element = this.local$tmp$.next();
            this.state_0 = 3;
            this.result_0 = this.$this.update_nqi3in$(element, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            this.state_0 = 2;
            continue;
          case 4:
            return this.local$forEach$result;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepository.prototype.updateMany_943sby$ = function (collection_0, continuation_0, suspended) {
    var instance = new Coroutine$updateMany_943sby$(this, collection_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ModelRepository.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'ModelRepository',
    interfaces: [ViewRepository]
  };
  function Coroutine$updateBy($receiver_0, q_0, updater_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$tmp$ = void 0;
    this.local$$receiver = $receiver_0;
    this.local$q = q_0;
    this.local$updater = updater_0;
  }
  Coroutine$updateBy.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$updateBy.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$updateBy.prototype.constructor = Coroutine$updateBy;
  Coroutine$updateBy.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$$receiver.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$tmp$ = this.result_0.iterator();
            this.state_0 = 3;
            continue;
          case 3:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 5;
              continue;
            }

            var it = this.local$tmp$.next();
            this.state_0 = 4;
            this.result_0 = this.local$$receiver.update_nqi3in$(this.local$updater(it), this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 4:
            this.state_0 = 3;
            continue;
          case 5:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function updateBy($receiver_0, q_0, updater_0, continuation_0, suspended) {
    var instance = new Coroutine$updateBy($receiver_0, q_0, updater_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  }
  var PAGE_MAX_SIZE;
  function Resultset(rows, next) {
    if (next === void 0)
      next = new Page();
    this.rows = rows;
    this.next = next;
  }
  Resultset.prototype.map_2o04qz$ = function (f) {
    var $receiver = this.rows;
    var destination = ArrayList_init(collectionSizeOrDefault($receiver, 10));
    var tmp$;
    tmp$ = $receiver.iterator();
    while (tmp$.hasNext()) {
      var item = tmp$.next();
      destination.add_11rb$(f(item));
    }
    return new Resultset(destination, this.next);
  };
  Resultset.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Resultset',
    interfaces: []
  };
  function Page(asc, limit, cursor) {
    if (asc === void 0)
      asc = true;
    if (limit === void 0)
      limit = PAGE_MAX_SIZE;
    if (cursor === void 0)
      cursor = null;
    this.asc = asc;
    this.limit = limit;
    this.cursor = cursor;
  }
  Page.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Page',
    interfaces: []
  };
  Page.prototype.component1 = function () {
    return this.asc;
  };
  Page.prototype.component2 = function () {
    return this.limit;
  };
  Page.prototype.component3 = function () {
    return this.cursor;
  };
  Page.prototype.copy_l5n9ty$ = function (asc, limit, cursor) {
    return new Page(asc === void 0 ? this.asc : asc, limit === void 0 ? this.limit : limit, cursor === void 0 ? this.cursor : cursor);
  };
  Page.prototype.toString = function () {
    return 'Page(asc=' + Kotlin.toString(this.asc) + (', limit=' + Kotlin.toString(this.limit)) + (', cursor=' + Kotlin.toString(this.cursor)) + ')';
  };
  Page.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.asc) | 0;
    result = result * 31 + Kotlin.hashCode(this.limit) | 0;
    result = result * 31 + Kotlin.hashCode(this.cursor) | 0;
    return result;
  };
  Page.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.asc, other.asc) && Kotlin.equals(this.limit, other.limit) && Kotlin.equals(this.cursor, other.cursor)))));
  };
  function Coroutine$toList(f_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$out = void 0;
    this.local$page = void 0;
    this.local$f = f_0;
  }
  Coroutine$toList.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$toList.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$toList.prototype.constructor = Coroutine$toList;
  Coroutine$toList.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.local$out = ArrayList_init_0();
            this.local$page = new Page();
            this.state_0 = 2;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = this.local$f(this.local$page, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            var rs = this.result_0;
            this.local$out.addAll_brywnq$(rs.rows);
            this.local$page = rs.next;
            if (this.local$page.cursor == null) {
              this.state_0 = 4;
              continue;
            }

            this.state_0 = 2;
            continue;
          case 4:
            return this.local$out;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function toList_0(f_0, continuation_0, suspended) {
    var instance = new Coroutine$toList(f_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  }
  function ViewRepository() {
  }
  ViewRepository.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'ViewRepository',
    interfaces: []
  };
  function ScoredModel(score, model) {
    this.score = score;
    this.model = model;
  }
  ScoredModel.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ScoredModel',
    interfaces: []
  };
  ScoredModel.prototype.component1 = function () {
    return this.score;
  };
  ScoredModel.prototype.component2 = function () {
    return this.model;
  };
  ScoredModel.prototype.copy_8wve6$ = function (score, model) {
    return new ScoredModel(score === void 0 ? this.score : score, model === void 0 ? this.model : model);
  };
  ScoredModel.prototype.toString = function () {
    return 'ScoredModel(score=' + Kotlin.toString(this.score) + (', model=' + Kotlin.toString(this.model)) + ')';
  };
  ScoredModel.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.score) | 0;
    result = result * 31 + Kotlin.hashCode(this.model) | 0;
    return result;
  };
  ScoredModel.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.score, other.score) && Kotlin.equals(this.model, other.model)))));
  };
  function ScoredViewRepository() {
  }
  ScoredViewRepository.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'ScoredViewRepository',
    interfaces: []
  };
  function ModelRepositoryMemory(modelClass, idField) {
    this.modelClass = modelClass;
    this.idField = idField;
    this.table = LinkedHashMap_init();
  }
  ModelRepositoryMemory.prototype.init = function (continuation) {
  };
  ModelRepositoryMemory.prototype.insert_nqi3in$ = function (t, continuation) {
    return this.update_nqi3in$(t, continuation);
  };
  ModelRepositoryMemory.prototype.update_nqi3in$ = function (t, continuation) {
    this.table.put_xwzc9p$(this.id_pcemjq$_0(t), t);
    return t;
  };
  function Coroutine$updateBy_nsmg2n$($this, t_0, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$tmp$ = void 0;
    this.local$t = t_0;
    this.local$q = q_0;
  }
  Coroutine$updateBy_nsmg2n$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$updateBy_nsmg2n$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$updateBy_nsmg2n$.prototype.constructor = Coroutine$updateBy_nsmg2n$;
  Coroutine$updateBy_nsmg2n$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$tmp$ = this.result_0.iterator();
            this.state_0 = 3;
            continue;
          case 3:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 5;
              continue;
            }

            var element = this.local$tmp$.next();
            this.state_0 = 4;
            this.result_0 = this.$this.update_nqi3in$(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 4:
            this.state_0 = 3;
            continue;
          case 5:
            return this.local$t;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepositoryMemory.prototype.updateBy_nsmg2n$ = function (t_0, q_0, continuation_0, suspended) {
    var instance = new Coroutine$updateBy_nsmg2n$(this, t_0, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ModelRepositoryMemory.prototype.delete_trkh7z$ = function (i, continuation) {
    this.table.remove_11rb$(i);
  };
  function Coroutine$deleteBy_kca49p$($this, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$forEach$result = void 0;
    this.local$tmp$ = void 0;
    this.local$q = q_0;
  }
  Coroutine$deleteBy_kca49p$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$deleteBy_kca49p$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$deleteBy_kca49p$.prototype.constructor = Coroutine$deleteBy_kca49p$;
  Coroutine$deleteBy_kca49p$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$tmp$ = this.result_0.iterator();
            this.state_0 = 3;
            continue;
          case 3:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 5;
              continue;
            }

            var element = this.local$tmp$.next();
            this.state_0 = 4;
            this.result_0 = this.$this.delete_trkh7z$(this.$this.idField.get(element), this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 4:
            this.state_0 = 3;
            continue;
          case 5:
            return this.local$forEach$result;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepositoryMemory.prototype.deleteBy_kca49p$ = function (q_0, continuation_0, suspended) {
    var instance = new Coroutine$deleteBy_kca49p$(this, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ModelRepositoryMemory.prototype.findById_trkh7z$ = function (i, continuation) {
    return this.table.get_11rb$(i);
  };
  function Coroutine$findOneBy_kca49p$($this, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$q = q_0;
  }
  Coroutine$findOneBy_kca49p$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$findOneBy_kca49p$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$findOneBy_kca49p$.prototype.constructor = Coroutine$findOneBy_kca49p$;
  Coroutine$findOneBy_kca49p$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return firstOrNull(this.result_0);
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepositoryMemory.prototype.findOneBy_kca49p$ = function (q_0, continuation_0, suspended) {
    var instance = new Coroutine$findOneBy_kca49p$(this, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ModelRepositoryMemory.prototype.findBy_kca49p$ = function (q, continuation) {
    var $receiver = this.table.values;
    var destination = ArrayList_init_0();
    var tmp$;
    tmp$ = $receiver.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      if (this.match_44bfwh$_0(element, q))
        destination.add_11rb$(element);
    }
    return destination;
  };
  ModelRepositoryMemory.prototype.getAll = function (continuation) {
    return toList(this.table.values);
  };
  function Coroutine$searchBy_kca49p$($this, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$q = q_0;
  }
  Coroutine$searchBy_kca49p$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$searchBy_kca49p$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$searchBy_kca49p$.prototype.constructor = Coroutine$searchBy_kca49p$;
  Coroutine$searchBy_kca49p$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            var $receiver = this.result_0;
            var destination = ArrayList_init(collectionSizeOrDefault($receiver, 10));
            var tmp$;
            tmp$ = $receiver.iterator();
            while (tmp$.hasNext()) {
              var item = tmp$.next();
              destination.add_11rb$(new ScoredModel(1.0, item));
            }

            return destination;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepositoryMemory.prototype.searchBy_kca49p$ = function (q_0, continuation_0, suspended) {
    var instance = new Coroutine$searchBy_kca49p$(this, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ModelRepositoryMemory.prototype.id_pcemjq$_0 = function (t) {
    return this.idField.get(t);
  };
  ModelRepositoryMemory.prototype.match_44bfwh$_0 = function ($receiver, q) {
    var tmp$, tmp$_0;
    if (Kotlin.isType(q, FieldUnop)) {
      if (Kotlin.isType(q, FieldIsNull))
        return q.field.get($receiver) == null;
      else
        throw new NotImplementedError('Missing implementation of .toSquash() for ' + $receiver);
    }
     else if (Kotlin.isType(q, FieldBinop)) {
      if (Kotlin.isType(q, FieldEqs))
        return equals(q.field.get($receiver), q.value);
      else if (Kotlin.isType(q, FieldLike))
        return (tmp$_0 = (tmp$ = q.field.get($receiver)) != null ? contains(tmp$, q.value) : null) != null ? tmp$_0 : false;
      else if (Kotlin.isType(q, FieldGt)) {
        var it = this.compare_l06vlq$_0($receiver, q);
        return it == null ? false : it > 0;
      }
       else if (Kotlin.isType(q, FieldGte)) {
        var it_0 = this.compare_l06vlq$_0($receiver, q);
        return it_0 == null ? false : it_0 >= 0;
      }
       else if (Kotlin.isType(q, FieldLt)) {
        var it_1 = this.compare_l06vlq$_0($receiver, q);
        return it_1 == null ? false : it_1 < 0;
      }
       else if (Kotlin.isType(q, FieldLte)) {
        var it_2 = this.compare_l06vlq$_0($receiver, q);
        return it_2 == null ? false : it_2 <= 0;
      }
       else if (Kotlin.isType(q, FieldWithin))
        return contains_0(q.value, q.field($receiver));
      else if (Kotlin.isType(q, FieldWithinComplex))
        return contains_0(q.value, q.field($receiver));
      else
        throw new NotImplementedError('Missing implementation of .toSquash() for ' + $receiver);
    }
     else if (Kotlin.isType(q, FilterExpAnd))
      return this.match_44bfwh$_0($receiver, q.left) & this.match_44bfwh$_0($receiver, q.right);
    else if (Kotlin.isType(q, FilterExpOr))
      return this.match_44bfwh$_0($receiver, q.left) | this.match_44bfwh$_0($receiver, q.right);
    else
      throw new NotImplementedError('Missing implementation of .toSquash() for ' + $receiver);
  };
  ModelRepositoryMemory.prototype.compare_l06vlq$_0 = function ($receiver, q) {
    var tmp$, tmp$_0, tmp$_1, tmp$_2;
    tmp$_2 = (tmp$ = q.field.get($receiver)) == null || Kotlin.isComparable(tmp$) ? tmp$ : throwCCE();
    tmp$_1 = Kotlin.isComparable(tmp$_0 = q.value) ? tmp$_0 : throwCCE();
    return tmp$_2 != null ? Kotlin.compareTo(tmp$_2, tmp$_1) : null;
  };
  ModelRepositoryMemory.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ModelRepositoryMemory',
    interfaces: [ScoredViewRepository, ModelRepository]
  };
  function AbstractViewRepositoryDecorator(repo) {
    this.repo = repo;
  }
  function Coroutine$init($this, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
  }
  Coroutine$init.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$init.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$init.prototype.constructor = Coroutine$init;
  Coroutine$init.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo.init(this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  AbstractViewRepositoryDecorator.prototype.init = function (continuation_0, suspended) {
    var instance = new Coroutine$init(this, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  AbstractViewRepositoryDecorator.prototype.getAll = function (continuation) {
    return this.repo.getAll(continuation);
  };
  AbstractViewRepositoryDecorator.prototype.findById_trkh7z$ = function (i, continuation) {
    return this.repo.findById_trkh7z$(i, continuation);
  };
  AbstractViewRepositoryDecorator.prototype.findOneBy_kca49p$ = function (q, continuation) {
    return this.repo.findOneBy_kca49p$(q, continuation);
  };
  AbstractViewRepositoryDecorator.prototype.findBy_kca49p$ = function (q, continuation) {
    return this.repo.findBy_kca49p$(q, continuation);
  };
  AbstractViewRepositoryDecorator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'AbstractViewRepositoryDecorator',
    interfaces: [ViewRepository]
  };
  function BusModelRepositoryDecorator(modelClass, repo, bus) {
    this.modelClass_7ehgc9$_0 = modelClass;
    this.repo_yh5c5m$_0 = repo;
    this.bus_5r5fik$_0 = bus;
  }
  function Coroutine$init_0($this, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
  }
  Coroutine$init_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$init_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$init_0.prototype.constructor = Coroutine$init_0;
  Coroutine$init_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo_yh5c5m$_0.init(this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  BusModelRepositoryDecorator.prototype.init = function (continuation_0, suspended) {
    var instance = new Coroutine$init_0(this, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  BusModelRepositoryDecorator.prototype.getAll = function (continuation) {
    return this.repo_yh5c5m$_0.getAll(continuation);
  };
  function Coroutine$insert_nqi3in$($this, t_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$t_0 = void 0;
    this.local$t = t_0;
  }
  Coroutine$insert_nqi3in$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$insert_nqi3in$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$insert_nqi3in$.prototype.constructor = Coroutine$insert_nqi3in$;
  Coroutine$insert_nqi3in$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo_yh5c5m$_0.insert_nqi3in$(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$t_0 = this.result_0;
            this.state_0 = 3;
            this.result_0 = this.$this.bus_5r5fik$_0.publishAsync_fym6v3$(changeEventTopic(this.$this.modelClass_7ehgc9$_0, ModelChangeType$INSERT_getInstance()), this.local$t_0, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.local$t_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  BusModelRepositoryDecorator.prototype.insert_nqi3in$ = function (t_0, continuation_0, suspended) {
    var instance = new Coroutine$insert_nqi3in$(this, t_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$update_nqi3in$($this, t_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$t_0 = void 0;
    this.local$t = t_0;
  }
  Coroutine$update_nqi3in$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$update_nqi3in$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$update_nqi3in$.prototype.constructor = Coroutine$update_nqi3in$;
  Coroutine$update_nqi3in$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo_yh5c5m$_0.update_nqi3in$(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$t_0 = this.result_0;
            this.state_0 = 3;
            this.result_0 = this.$this.bus_5r5fik$_0.publishAsync_fym6v3$(changeEventTopic(this.$this.modelClass_7ehgc9$_0, ModelChangeType$UPDATE_getInstance()), this.local$t_0, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.local$t_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  BusModelRepositoryDecorator.prototype.update_nqi3in$ = function (t_0, continuation_0, suspended) {
    var instance = new Coroutine$update_nqi3in$(this, t_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  BusModelRepositoryDecorator.prototype.updateBy_nsmg2n$ = function (t, q, continuation) {
    throw new NotImplementedError('An operation is not implemented: ' + 'not implemented');
  };
  function Coroutine$delete_trkh7z$($this, i_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$i = i_0;
  }
  Coroutine$delete_trkh7z$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$delete_trkh7z$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$delete_trkh7z$.prototype.constructor = Coroutine$delete_trkh7z$;
  Coroutine$delete_trkh7z$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo_yh5c5m$_0.delete_trkh7z$(this.local$i, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = this.$this.bus_5r5fik$_0.publishAsync_fym6v3$(changeEventTopic(this.$this.modelClass_7ehgc9$_0, ModelChangeType$DELETE_getInstance()), this.local$i, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  BusModelRepositoryDecorator.prototype.delete_trkh7z$ = function (i_0, continuation_0, suspended) {
    var instance = new Coroutine$delete_trkh7z$(this, i_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  BusModelRepositoryDecorator.prototype.deleteBy_kca49p$ = function (q, continuation) {
    throw new NotImplementedError('An operation is not implemented: ' + 'not implemented');
  };
  BusModelRepositoryDecorator.prototype.findById_trkh7z$ = function (i, continuation) {
    return this.repo_yh5c5m$_0.findById_trkh7z$(i, continuation);
  };
  BusModelRepositoryDecorator.prototype.findOneBy_kca49p$ = function (q, continuation) {
    return this.repo_yh5c5m$_0.findOneBy_kca49p$(q, continuation);
  };
  BusModelRepositoryDecorator.prototype.findBy_kca49p$ = function (q, continuation) {
    return this.repo_yh5c5m$_0.findBy_kca49p$(q, continuation);
  };
  BusModelRepositoryDecorator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'BusModelRepositoryDecorator',
    interfaces: [ModelRepository]
  };
  function CQRSModelRepositoryDecorator(mainRepo, queryRepo) {
    this.mainRepo_yzkfxg$_0 = mainRepo;
    this.queryRepo_96wiy3$_0 = queryRepo;
  }
  function Coroutine$init_1($this, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
  }
  Coroutine$init_1.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$init_1.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$init_1.prototype.constructor = Coroutine$init_1;
  Coroutine$init_1.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.mainRepo_yzkfxg$_0.init(this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = this.$this.queryRepo_96wiy3$_0.init(this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CQRSModelRepositoryDecorator.prototype.init = function (continuation_0, suspended) {
    var instance = new Coroutine$init_1(this, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  CQRSModelRepositoryDecorator.prototype.getAll = function (continuation) {
    return this.queryRepo_96wiy3$_0.getAll(continuation);
  };
  function Coroutine$insert_nqi3in$_0($this, t_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$t_0 = void 0;
    this.local$t = t_0;
  }
  Coroutine$insert_nqi3in$_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$insert_nqi3in$_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$insert_nqi3in$_0.prototype.constructor = Coroutine$insert_nqi3in$_0;
  Coroutine$insert_nqi3in$_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.mainRepo_yzkfxg$_0.insert_nqi3in$(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$t_0 = this.result_0;
            this.state_0 = 3;
            this.result_0 = this.$this.queryRepo_96wiy3$_0.insert_nqi3in$(this.local$t_0, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.local$t_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CQRSModelRepositoryDecorator.prototype.insert_nqi3in$ = function (t_0, continuation_0, suspended) {
    var instance = new Coroutine$insert_nqi3in$_0(this, t_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$update_nqi3in$_0($this, t_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$t_0 = void 0;
    this.local$t = t_0;
  }
  Coroutine$update_nqi3in$_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$update_nqi3in$_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$update_nqi3in$_0.prototype.constructor = Coroutine$update_nqi3in$_0;
  Coroutine$update_nqi3in$_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.mainRepo_yzkfxg$_0.update_nqi3in$(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$t_0 = this.result_0;
            this.state_0 = 3;
            this.result_0 = this.$this.queryRepo_96wiy3$_0.update_nqi3in$(this.local$t_0, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.local$t_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CQRSModelRepositoryDecorator.prototype.update_nqi3in$ = function (t_0, continuation_0, suspended) {
    var instance = new Coroutine$update_nqi3in$_0(this, t_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  CQRSModelRepositoryDecorator.prototype.updateBy_nsmg2n$ = function (t, q, continuation) {
    throw new NotImplementedError('An operation is not implemented: ' + 'not implemented');
  };
  function Coroutine$delete_trkh7z$_0($this, i_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$i = i_0;
  }
  Coroutine$delete_trkh7z$_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$delete_trkh7z$_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$delete_trkh7z$_0.prototype.constructor = Coroutine$delete_trkh7z$_0;
  Coroutine$delete_trkh7z$_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.mainRepo_yzkfxg$_0.delete_trkh7z$(this.local$i, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = this.$this.queryRepo_96wiy3$_0.delete_trkh7z$(this.local$i, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CQRSModelRepositoryDecorator.prototype.delete_trkh7z$ = function (i_0, continuation_0, suspended) {
    var instance = new Coroutine$delete_trkh7z$_0(this, i_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$deleteBy_kca49p$_0($this, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$q = q_0;
  }
  Coroutine$deleteBy_kca49p$_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$deleteBy_kca49p$_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$deleteBy_kca49p$_0.prototype.constructor = Coroutine$deleteBy_kca49p$_0;
  Coroutine$deleteBy_kca49p$_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.mainRepo_yzkfxg$_0.deleteBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = this.$this.queryRepo_96wiy3$_0.deleteBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CQRSModelRepositoryDecorator.prototype.deleteBy_kca49p$ = function (q_0, continuation_0, suspended) {
    var instance = new Coroutine$deleteBy_kca49p$_0(this, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  CQRSModelRepositoryDecorator.prototype.findById_trkh7z$ = function (i, continuation) {
    return this.queryRepo_96wiy3$_0.findById_trkh7z$(i, continuation);
  };
  CQRSModelRepositoryDecorator.prototype.findOneBy_kca49p$ = function (q, continuation) {
    return this.queryRepo_96wiy3$_0.findOneBy_kca49p$(q, continuation);
  };
  CQRSModelRepositoryDecorator.prototype.findBy_kca49p$ = function (q, continuation) {
    return this.queryRepo_96wiy3$_0.findBy_kca49p$(q, continuation);
  };
  CQRSModelRepositoryDecorator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CQRSModelRepositoryDecorator',
    interfaces: [ModelRepository]
  };
  function Cache() {
  }
  Cache.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'Cache',
    interfaces: []
  };
  function CachedModelRepository(modelClass, idField, repo, cache, cacheField) {
    ModelRepositoryDecorator.call(this, repo);
    this.modelClass = modelClass;
    this.idField = idField;
    this.repo = repo;
    this.cache_0 = cache;
    this.cacheField_0 = cacheField;
  }
  CachedModelRepository.prototype.invalidate_0 = function (t, continuation) {
    return this.cache_0.remove_61zpoe$(toString(this.cacheField_0(t)), continuation);
  };
  function Coroutine$insert_nqi3in$_1($this, t_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$t = t_0;
  }
  Coroutine$insert_nqi3in$_1.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$insert_nqi3in$_1.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$insert_nqi3in$_1.prototype.constructor = Coroutine$insert_nqi3in$_1;
  Coroutine$insert_nqi3in$_1.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.invalidate_0(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = ModelRepositoryDecorator.prototype.insert_nqi3in$.call(this.$this, this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CachedModelRepository.prototype.insert_nqi3in$ = function (t_0, continuation_0, suspended) {
    var instance = new Coroutine$insert_nqi3in$_1(this, t_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$update_nqi3in$_1($this, t_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$t = t_0;
  }
  Coroutine$update_nqi3in$_1.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$update_nqi3in$_1.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$update_nqi3in$_1.prototype.constructor = Coroutine$update_nqi3in$_1;
  Coroutine$update_nqi3in$_1.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.invalidate_0(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.state_0 = 3;
            this.result_0 = ModelRepositoryDecorator.prototype.update_nqi3in$.call(this.$this, this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CachedModelRepository.prototype.update_nqi3in$ = function (t_0, continuation_0, suspended) {
    var instance = new Coroutine$update_nqi3in$_1(this, t_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$delete_trkh7z$_1($this, i_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$i = i_0;
  }
  Coroutine$delete_trkh7z$_1.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$delete_trkh7z$_1.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$delete_trkh7z$_1.prototype.constructor = Coroutine$delete_trkh7z$_1;
  Coroutine$delete_trkh7z$_1.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            var tmp$;
            this.state_0 = 2;
            this.result_0 = this.$this.findById_trkh7z$(this.local$i, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            tmp$ = this.result_0;
            if (tmp$ == null) {
              throw IllegalArgumentException_init();
            }

            var t = tmp$;
            this.state_0 = 3;
            this.result_0 = this.$this.invalidate_0(t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 3:
            this.state_0 = 4;
            this.result_0 = ModelRepositoryDecorator.prototype.delete_trkh7z$.call(this.$this, this.local$i, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 4:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CachedModelRepository.prototype.delete_trkh7z$ = function (i_0, continuation_0, suspended) {
    var instance = new Coroutine$delete_trkh7z$_1(this, i_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  function Coroutine$findBy_kca49p$($this, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$tmp$ = void 0;
    this.local$key = void 0;
    this.local$subset = void 0;
    this.local$subRepo = void 0;
    this.local$tmp$_0 = void 0;
    this.local$q = q_0;
  }
  Coroutine$findBy_kca49p$.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$findBy_kca49p$.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$findBy_kca49p$.prototype.constructor = Coroutine$findBy_kca49p$;
  Coroutine$findBy_kca49p$.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            var keyEq = this.$this.findCacheQuery_0(this.local$q);
            if (keyEq != null) {
              this.local$key = keyEq.value.toString();
              this.state_0 = 3;
              this.result_0 = this.$this.cache_0.get_3zqiyt$(this.local$key, this);
              if (this.result_0 === COROUTINE_SUSPENDED)
                return COROUTINE_SUSPENDED;
              continue;
            }
             else {
              this.state_0 = 2;
              this.result_0 = ModelRepositoryDecorator.prototype.findBy_kca49p$.call(this.$this, this.local$q, this);
              if (this.result_0 === COROUTINE_SUSPENDED)
                return COROUTINE_SUSPENDED;
              continue;
            }

          case 1:
            throw this.exception_0;
          case 2:
            this.local$tmp$ = this.result_0;
            this.state_0 = 11;
            continue;
          case 3:
            this.local$subset = this.result_0;
            if (this.local$subset == null) {
              this.state_0 = 4;
              this.result_0 = ModelRepositoryDecorator.prototype.findBy_kca49p$.call(this.$this, this.local$q, this);
              if (this.result_0 === COROUTINE_SUSPENDED)
                return COROUTINE_SUSPENDED;
              continue;
            }
             else {
              this.state_0 = 6;
              continue;
            }

          case 4:
            this.local$subset = this.result_0;
            this.state_0 = 5;
            this.result_0 = this.$this.cache_0.put_fym6v3$(this.local$key, this.local$subset, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 5:
            this.state_0 = 6;
            continue;
          case 6:
            this.local$subRepo = new ModelRepositoryMemory(this.$this.modelClass, this.$this.idField);
            this.local$tmp$_0 = this.local$subset.iterator();
            this.state_0 = 7;
            continue;
          case 7:
            if (!this.local$tmp$_0.hasNext()) {
              this.state_0 = 9;
              continue;
            }

            var element = this.local$tmp$_0.next();
            this.state_0 = 8;
            this.result_0 = this.local$subRepo.insert_nqi3in$(element, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 8:
            this.state_0 = 7;
            continue;
          case 9:
            this.state_0 = 10;
            this.result_0 = this.local$subRepo.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 10:
            this.local$tmp$ = this.result_0;
            this.state_0 = 11;
            continue;
          case 11:
            return this.local$tmp$;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  CachedModelRepository.prototype.findBy_kca49p$ = function (q_0, continuation_0, suspended) {
    var instance = new Coroutine$findBy_kca49p$(this, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  CachedModelRepository.prototype.findCacheQuery_0 = function (q) {
    var tmp$;
    if (Kotlin.isType(q, FieldEqs) && equals(q.field, this.cacheField_0))
      return q;
    else if (Kotlin.isType(q, FilterExpAnd))
      return (tmp$ = this.findCacheQuery_0(q.left)) != null ? tmp$ : this.findCacheQuery_0(q.right);
    else
      throw new NotImplementedError('Missing hadling of query type: ' + q);
  };
  CachedModelRepository.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CachedModelRepository',
    interfaces: [ModelRepositoryDecorator]
  };
  function FastEventuallyConsistentModelRepositoryDecorator(modelClass, repo, bus) {
    BusModelRepositoryDecorator.call(this, modelClass, new NullModelRepository(), bus);
    this.modelClass_0 = modelClass;
    this.repo_0 = repo;
    this.bus_0 = bus;
  }
  function Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda(this$FastEventuallyConsistentModelRepositoryDecorator_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$this$FastEventuallyConsistentModelRepositoryDecorator = this$FastEventuallyConsistentModelRepositoryDecorator_0;
    this.local$it = it_0;
  }
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda.prototype.constructor = Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda;
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$this$FastEventuallyConsistentModelRepositoryDecorator.repo_0.insert_nqi3in$(this.local$it, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function FastEventuallyConsistentModelRepositoryDecorator$init$lambda(this$FastEventuallyConsistentModelRepositoryDecorator_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda(this$FastEventuallyConsistentModelRepositoryDecorator_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0(this$FastEventuallyConsistentModelRepositoryDecorator_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$this$FastEventuallyConsistentModelRepositoryDecorator = this$FastEventuallyConsistentModelRepositoryDecorator_0;
    this.local$it = it_0;
  }
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0.prototype.constructor = Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0;
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$this$FastEventuallyConsistentModelRepositoryDecorator.repo_0.update_nqi3in$(this.local$it, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0(this$FastEventuallyConsistentModelRepositoryDecorator_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0(this$FastEventuallyConsistentModelRepositoryDecorator_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1(this$FastEventuallyConsistentModelRepositoryDecorator_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$this$FastEventuallyConsistentModelRepositoryDecorator = this$FastEventuallyConsistentModelRepositoryDecorator_0;
    this.local$it = it_0;
  }
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1.prototype.constructor = Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1;
  Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$this$FastEventuallyConsistentModelRepositoryDecorator.repo_0.delete_trkh7z$(this.local$it, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1(this$FastEventuallyConsistentModelRepositoryDecorator_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1(this$FastEventuallyConsistentModelRepositoryDecorator_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$init_2($this, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
  }
  Coroutine$init_2.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$init_2.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$init_2.prototype.constructor = Coroutine$init_2;
  Coroutine$init_2.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo_0.init(this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.$this.bus_0.registerAsync_fcawk$(changeEventTopic(this.$this.modelClass_0, ModelChangeType$INSERT_getInstance()), FastEventuallyConsistentModelRepositoryDecorator$init$lambda(this.$this));
            this.$this.bus_0.registerAsync_fcawk$(changeEventTopic(this.$this.modelClass_0, ModelChangeType$UPDATE_getInstance()), FastEventuallyConsistentModelRepositoryDecorator$init$lambda_0(this.$this));
            this.$this.bus_0.registerAsync_fcawk$(changeEventTopic(this.$this.modelClass_0, ModelChangeType$DELETE_getInstance()), FastEventuallyConsistentModelRepositoryDecorator$init$lambda_1(this.$this));
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  FastEventuallyConsistentModelRepositoryDecorator.prototype.init = function (continuation_0, suspended) {
    var instance = new Coroutine$init_2(this, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  FastEventuallyConsistentModelRepositoryDecorator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FastEventuallyConsistentModelRepositoryDecorator',
    interfaces: [BusModelRepositoryDecorator]
  };
  function ModelChangeType(name, ordinal) {
    Enum.call(this);
    this.name$ = name;
    this.ordinal$ = ordinal;
  }
  function ModelChangeType_initFields() {
    ModelChangeType_initFields = function () {
    };
    ModelChangeType$INSERT_instance = new ModelChangeType('INSERT', 0);
    ModelChangeType$UPDATE_instance = new ModelChangeType('UPDATE', 1);
    ModelChangeType$DELETE_instance = new ModelChangeType('DELETE', 2);
    ModelChangeType$INIT_instance = new ModelChangeType('INIT', 3);
  }
  var ModelChangeType$INSERT_instance;
  function ModelChangeType$INSERT_getInstance() {
    ModelChangeType_initFields();
    return ModelChangeType$INSERT_instance;
  }
  var ModelChangeType$UPDATE_instance;
  function ModelChangeType$UPDATE_getInstance() {
    ModelChangeType_initFields();
    return ModelChangeType$UPDATE_instance;
  }
  var ModelChangeType$DELETE_instance;
  function ModelChangeType$DELETE_getInstance() {
    ModelChangeType_initFields();
    return ModelChangeType$DELETE_instance;
  }
  var ModelChangeType$INIT_instance;
  function ModelChangeType$INIT_getInstance() {
    ModelChangeType_initFields();
    return ModelChangeType$INIT_instance;
  }
  ModelChangeType.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ModelChangeType',
    interfaces: [Enum]
  };
  function ModelChangeType$values() {
    return [ModelChangeType$INSERT_getInstance(), ModelChangeType$UPDATE_getInstance(), ModelChangeType$DELETE_getInstance(), ModelChangeType$INIT_getInstance()];
  }
  ModelChangeType.values = ModelChangeType$values;
  function ModelChangeType$valueOf(name) {
    switch (name) {
      case 'INSERT':
        return ModelChangeType$INSERT_getInstance();
      case 'UPDATE':
        return ModelChangeType$UPDATE_getInstance();
      case 'DELETE':
        return ModelChangeType$DELETE_getInstance();
      case 'INIT':
        return ModelChangeType$INIT_getInstance();
      default:throwISE('No enum constant kuick.repositories.patterns.ModelChangeType.' + name);
    }
  }
  ModelChangeType.valueOf_61zpoe$ = ModelChangeType$valueOf;
  function changeEventTopic($receiver, type) {
    return toString($receiver.simpleName) + '/' + type.name;
  }
  function publishInsert($receiver, ev, continuation) {
    return $receiver.publishAsync_fym6v3$(changeEventTopic(Kotlin.getKClassFromExpression(ev), ModelChangeType$INSERT_getInstance()), ev, continuation);
  }
  function publishUpdate($receiver, ev, continuation) {
    return $receiver.publishAsync_fym6v3$(changeEventTopic(Kotlin.getKClassFromExpression(ev), ModelChangeType$UPDATE_getInstance()), ev, continuation);
  }
  function ModelRepositoryDecorator(repo) {
    this.repo_iyr442$_0 = repo;
  }
  ModelRepositoryDecorator.prototype.insert_nqi3in$ = function (t, continuation) {
    return this.repo_iyr442$_0.insert_nqi3in$(t, continuation);
  };
  ModelRepositoryDecorator.prototype.update_nqi3in$ = function (t, continuation) {
    return this.repo_iyr442$_0.update_nqi3in$(t, continuation);
  };
  function Coroutine$updateBy_nsmg2n$_0($this, t_0, q_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
    this.local$tmp$ = void 0;
    this.local$t = t_0;
    this.local$q = q_0;
  }
  Coroutine$updateBy_nsmg2n$_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$updateBy_nsmg2n$_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$updateBy_nsmg2n$_0.prototype.constructor = Coroutine$updateBy_nsmg2n$_0;
  Coroutine$updateBy_nsmg2n$_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.findBy_kca49p$(this.local$q, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            this.local$tmp$ = this.result_0.iterator();
            this.state_0 = 3;
            continue;
          case 3:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 5;
              continue;
            }

            var element = this.local$tmp$.next();
            this.state_0 = 4;
            this.result_0 = this.$this.update_nqi3in$(this.local$t, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 4:
            this.state_0 = 3;
            continue;
          case 5:
            return this.local$t;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ModelRepositoryDecorator.prototype.updateBy_nsmg2n$ = function (t_0, q_0, continuation_0, suspended) {
    var instance = new Coroutine$updateBy_nsmg2n$_0(this, t_0, q_0, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ModelRepositoryDecorator.prototype.delete_trkh7z$ = function (i, continuation) {
    return this.repo_iyr442$_0.delete_trkh7z$(i, continuation);
  };
  ModelRepositoryDecorator.prototype.deleteBy_kca49p$ = function (q, continuation) {
    return this.repo_iyr442$_0.deleteBy_kca49p$(q, continuation);
  };
  ModelRepositoryDecorator.prototype.init = function (continuation) {
    return this.repo_iyr442$_0.init(continuation);
  };
  ModelRepositoryDecorator.prototype.findById_trkh7z$ = function (i, continuation) {
    return this.repo_iyr442$_0.findById_trkh7z$(i, continuation);
  };
  ModelRepositoryDecorator.prototype.findOneBy_kca49p$ = function (q, continuation) {
    return this.repo_iyr442$_0.findOneBy_kca49p$(q, continuation);
  };
  ModelRepositoryDecorator.prototype.findBy_kca49p$ = function (q, continuation) {
    return this.repo_iyr442$_0.findBy_kca49p$(q, continuation);
  };
  ModelRepositoryDecorator.prototype.getAll = function (continuation) {
    return this.repo_iyr442$_0.getAll(continuation);
  };
  ModelRepositoryDecorator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ModelRepositoryDecorator',
    interfaces: [ModelRepository]
  };
  function NullModelRepository() {
  }
  NullModelRepository.prototype.insert_nqi3in$ = function (t, continuation) {
    return t;
  };
  NullModelRepository.prototype.update_nqi3in$ = function (t, continuation) {
    return t;
  };
  NullModelRepository.prototype.updateBy_nsmg2n$ = function (t, q, continuation) {
    return t;
  };
  NullModelRepository.prototype.delete_trkh7z$ = function (i, continuation) {
  };
  NullModelRepository.prototype.deleteBy_kca49p$ = function (q, continuation) {
  };
  NullModelRepository.prototype.init = function (continuation) {
  };
  NullModelRepository.prototype.getAll = function (continuation) {
    return emptyList();
  };
  NullModelRepository.prototype.findById_trkh7z$ = function (i, continuation) {
    return null;
  };
  NullModelRepository.prototype.findOneBy_kca49p$ = function (q, continuation) {
    return null;
  };
  NullModelRepository.prototype.findBy_kca49p$ = function (q, continuation) {
    return emptyList();
  };
  NullModelRepository.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'NullModelRepository',
    interfaces: [ModelRepository]
  };
  function ViewRepositoryDecorator(repo, bus, listeners) {
    AbstractViewRepositoryDecorator.call(this, repo);
    this.bus_ael1y0$_0 = bus;
    this.listeners = listeners;
  }
  function Coroutine$ViewRepositoryDecorator$init$lambda$updater(this$ViewRepositoryDecorator_0, closure$fkl_0, foreignModel_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$this$ViewRepositoryDecorator = this$ViewRepositoryDecorator_0;
    this.local$closure$fkl = closure$fkl_0;
    this.local$tmp$ = void 0;
    this.local$this$ViewRepositoryDecorator_0 = void 0;
    this.local$foreignModel = foreignModel_0;
  }
  Coroutine$ViewRepositoryDecorator$init$lambda$updater.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$ViewRepositoryDecorator$init$lambda$updater.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$ViewRepositoryDecorator$init$lambda$updater.prototype.constructor = Coroutine$ViewRepositoryDecorator$init$lambda$updater;
  Coroutine$ViewRepositoryDecorator$init$lambda$updater.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$this$ViewRepositoryDecorator.repo.findBy_kca49p$(this.local$closure$fkl.selector(this.local$foreignModel), this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            var affectedViews = this.result_0;
            if (affectedViews.isEmpty()) {
              this.state_0 = 7;
              this.result_0 = this.local$closure$fkl.listener(null, this.local$foreignModel, this);
              if (this.result_0 === COROUTINE_SUSPENDED)
                return COROUTINE_SUSPENDED;
              continue;
            }
             else {
              this.local$tmp$ = affectedViews.iterator();
              this.state_0 = 3;
              continue;
            }

          case 3:
            if (!this.local$tmp$.hasNext()) {
              this.state_0 = 6;
              continue;
            }

            var element = this.local$tmp$.next();
            var closure$fkl = this.local$closure$fkl;
            this.local$this$ViewRepositoryDecorator_0 = this.local$this$ViewRepositoryDecorator;
            this.state_0 = 4;
            this.result_0 = closure$fkl.listener(element, this.local$foreignModel, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 4:
            var updatedViews = this.result_0;
            this.state_0 = 5;
            this.result_0 = this.local$this$ViewRepositoryDecorator_0.repo.update_nqi3in$(updatedViews, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 5:
            this.state_0 = 3;
            continue;
          case 6:
            this.state_0 = 9;
            continue;
          case 7:
            var newView = this.result_0;
            this.state_0 = 8;
            this.result_0 = this.local$this$ViewRepositoryDecorator.repo.insert_nqi3in$(newView, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 8:
            this.state_0 = 9;
            continue;
          case 9:
            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function ViewRepositoryDecorator$init$lambda$updater(this$ViewRepositoryDecorator_0, closure$fkl_0) {
    return function (foreignModel_0, continuation_0, suspended) {
      var instance = new Coroutine$ViewRepositoryDecorator$init$lambda$updater(this$ViewRepositoryDecorator_0, closure$fkl_0, foreignModel_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$ViewRepositoryDecorator$init$lambda$lambda(closure$updater_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$closure$updater = closure$updater_0;
    this.local$it = it_0;
  }
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda.prototype.constructor = Coroutine$ViewRepositoryDecorator$init$lambda$lambda;
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$closure$updater(this.local$it, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function ViewRepositoryDecorator$init$lambda$lambda(closure$updater_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$ViewRepositoryDecorator$init$lambda$lambda(closure$updater_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0(closure$updater_0, it_0, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.local$closure$updater = closure$updater_0;
    this.local$it = it_0;
  }
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0.prototype.constructor = Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0;
  Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.local$closure$updater(this.local$it, this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            return this.result_0;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  function ViewRepositoryDecorator$init$lambda$lambda_0(closure$updater_0) {
    return function (it_0, continuation_0, suspended) {
      var instance = new Coroutine$ViewRepositoryDecorator$init$lambda$lambda_0(closure$updater_0, it_0, continuation_0);
      if (suspended)
        return instance;
      else
        return instance.doResume(null);
    };
  }
  function Coroutine$init_3($this, continuation_0) {
    CoroutineImpl.call(this, continuation_0);
    this.exceptionState_0 = 1;
    this.$this = $this;
  }
  Coroutine$init_3.$metadata$ = {
    kind: Kotlin.Kind.CLASS,
    simpleName: null,
    interfaces: [CoroutineImpl]
  };
  Coroutine$init_3.prototype = Object.create(CoroutineImpl.prototype);
  Coroutine$init_3.prototype.constructor = Coroutine$init_3;
  Coroutine$init_3.prototype.doResume = function () {
    do
      try {
        switch (this.state_0) {
          case 0:
            this.state_0 = 2;
            this.result_0 = this.$this.repo.init(this);
            if (this.result_0 === COROUTINE_SUSPENDED)
              return COROUTINE_SUSPENDED;
            continue;
          case 1:
            throw this.exception_0;
          case 2:
            var tmp$;
            tmp$ = this.$this.listeners.updaters.iterator();
            while (tmp$.hasNext()) {
              var element = tmp$.next();
              var foreignModelKClass = element.foreignModel;
              var updater = ViewRepositoryDecorator$init$lambda$updater(this.$this, element);
              this.$this.bus_ael1y0$_0.registerAsync_fcawk$(changeEventTopic(foreignModelKClass, ModelChangeType$UPDATE_getInstance()), ViewRepositoryDecorator$init$lambda$lambda(updater));
              this.$this.bus_ael1y0$_0.registerAsync_fcawk$(changeEventTopic(foreignModelKClass, ModelChangeType$INSERT_getInstance()), ViewRepositoryDecorator$init$lambda$lambda_0(updater));
            }

            return;
          default:this.state_0 = 1;
            throw new Error('State Machine Unreachable execution');
        }
      }
       catch (e) {
        if (this.state_0 === 1) {
          this.exceptionState_0 = this.state_0;
          throw e;
        }
         else {
          this.state_0 = this.exceptionState_0;
          this.exception_0 = e;
        }
      }
     while (true);
  };
  ViewRepositoryDecorator.prototype.init = function (continuation_0, suspended) {
    var instance = new Coroutine$init_3(this, continuation_0);
    if (suspended)
      return instance;
    else
      return instance.doResume(null);
  };
  ViewRepositoryDecorator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ViewRepositoryDecorator',
    interfaces: [AbstractViewRepositoryDecorator]
  };
  function ViewListeners(builders, updaters) {
    if (builders === void 0)
      builders = emptyList();
    if (updaters === void 0)
      updaters = emptyList();
    this.builders = builders;
    this.updaters = updaters;
  }
  ViewListeners.prototype.updatesOn_9lqykn$ = function (foreignModel, selector, listener) {
    var tmp$;
    return this.copy_q4hgp6$(void 0, plus_0(this.updaters, Kotlin.isType(tmp$ = new Updater(foreignModel, selector, listener), Updater) ? tmp$ : throwCCE()));
  };
  ViewListeners.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ViewListeners',
    interfaces: []
  };
  ViewListeners.prototype.component1 = function () {
    return this.builders;
  };
  ViewListeners.prototype.component2 = function () {
    return this.updaters;
  };
  ViewListeners.prototype.copy_q4hgp6$ = function (builders, updaters) {
    return new ViewListeners(builders === void 0 ? this.builders : builders, updaters === void 0 ? this.updaters : updaters);
  };
  ViewListeners.prototype.toString = function () {
    return 'ViewListeners(builders=' + Kotlin.toString(this.builders) + (', updaters=' + Kotlin.toString(this.updaters)) + ')';
  };
  ViewListeners.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.builders) | 0;
    result = result * 31 + Kotlin.hashCode(this.updaters) | 0;
    return result;
  };
  ViewListeners.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.builders, other.builders) && Kotlin.equals(this.updaters, other.updaters)))));
  };
  function Builder(foreignModel, listener) {
    this.foreignModel = foreignModel;
    this.listener = listener;
  }
  Builder.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Builder',
    interfaces: []
  };
  Builder.prototype.component1 = function () {
    return this.foreignModel;
  };
  Builder.prototype.component2 = function () {
    return this.listener;
  };
  Builder.prototype.copy_ckepzr$ = function (foreignModel, listener) {
    return new Builder(foreignModel === void 0 ? this.foreignModel : foreignModel, listener === void 0 ? this.listener : listener);
  };
  Builder.prototype.toString = function () {
    return 'Builder(foreignModel=' + Kotlin.toString(this.foreignModel) + (', listener=' + Kotlin.toString(this.listener)) + ')';
  };
  Builder.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.foreignModel) | 0;
    result = result * 31 + Kotlin.hashCode(this.listener) | 0;
    return result;
  };
  Builder.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.foreignModel, other.foreignModel) && Kotlin.equals(this.listener, other.listener)))));
  };
  function Updater(foreignModel, selector, listener) {
    this.foreignModel = foreignModel;
    this.selector = selector;
    this.listener = listener;
  }
  Updater.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Updater',
    interfaces: []
  };
  Updater.prototype.component1 = function () {
    return this.foreignModel;
  };
  Updater.prototype.component2 = function () {
    return this.selector;
  };
  Updater.prototype.component3 = function () {
    return this.listener;
  };
  Updater.prototype.copy_z5qo5n$ = function (foreignModel, selector, listener) {
    return new Updater(foreignModel === void 0 ? this.foreignModel : foreignModel, selector === void 0 ? this.selector : selector, listener === void 0 ? this.listener : listener);
  };
  Updater.prototype.toString = function () {
    return 'Updater(foreignModel=' + Kotlin.toString(this.foreignModel) + (', selector=' + Kotlin.toString(this.selector)) + (', listener=' + Kotlin.toString(this.listener)) + ')';
  };
  Updater.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.foreignModel) | 0;
    result = result * 31 + Kotlin.hashCode(this.selector) | 0;
    result = result * 31 + Kotlin.hashCode(this.listener) | 0;
    return result;
  };
  Updater.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.foreignModel, other.foreignModel) && Kotlin.equals(this.selector, other.selector) && Kotlin.equals(this.listener, other.listener)))));
  };
  var package$kotlin = _.kotlin || (_.kotlin = {});
  var package$internal = package$kotlin.internal || (package$kotlin.internal = {});
  package$internal.Exact = Exact;
  var package$kuick = _.kuick || (_.kuick = {});
  var package$annotations = package$kuick.annotations || (package$kuick.annotations = {});
  package$annotations.WithCache = WithCache;
  package$annotations.toInfo_nbcios$ = toInfo;
  package$annotations.Cached = Cached;
  package$annotations.CacheInfo = CacheInfo;
  package$annotations.toInfo_wvinks$ = toInfo_0;
  package$annotations.InvalidatesCache = InvalidatesCache;
  var package$bus = package$kuick.bus || (package$kuick.bus = {});
  package$bus.Bus = Bus;
  package$bus.SyncBus = SyncBus;
  var package$caching = package$kuick.caching || (package$kuick.caching = {});
  package$caching.CacheManager = CacheManager;
  package$caching.getOrPutSuspendable_a0skig$ = getOrPutSuspendable;
  package$caching.CachedRepository = CachedRepository;
  var package$models = package$kuick.models || (package$kuick.models = {});
  package$models.Email = Email;
  package$models.Id = Id;
  package$models.IdProvider = IdProvider;
  package$models.parentPath_pdl1vz$ = parentPath;
  package$models.pathToSectionNumber_61zpoe$ = pathToSectionNumber;
  package$models.sectionNumberToPath_61zpoe$ = sectionNumberToPath;
  Object.defineProperty(package$models, 'K_LOCAL_DATE_FORMAT', {
    get: function () {
      return K_LOCAL_DATE_FORMAT;
    }
  });
  Object.defineProperty(package$models, 'K_LOCAL_DATE_TIME_FORMAT', {
    get: function () {
      return K_LOCAL_DATE_TIME_FORMAT;
    }
  });
  package$models.KLocalDate = KLocalDate;
  package$models.KDateRange = KDateRange;
  package$models.normalized_buitff$ = normalized;
  package$models.TimeService = TimeService;
  package$models.daysBetween_yi3whv$ = daysBetween;
  package$models.secondsToHuman_za3lpa$ = secondsToHuman;
  package$models.toPercent_yrwdxr$ = toPercent;
  package$models.NumberedTree = NumberedTree;
  package$models.findByNumber_kqduhr$ = findByNumber;
  package$models.parentNumber_pdl1vz$ = parentNumber;
  package$models.isRoot_7csoxd$ = isRoot;
  package$models.hasChildren_7csoxd$ = hasChildren;
  package$models.flat_8st17p$ = flat;
  package$models.flat_7csoxd$ = flat_0;
  package$models.findNextByNumber_yt64rr$ = findNextByNumber;
  package$models.findPreviousByNumber_yt64rr$ = findPreviousByNumber;
  package$models.findByNumberAndOffset_kff5hn$ = findByNumberAndOffset;
  var package$repositories = package$kuick.repositories || (package$kuick.repositories = {});
  package$repositories.ModelQuery = ModelQuery;
  package$repositories.ModelFilterExp = ModelFilterExp;
  package$repositories.FilterExpNot = FilterExpNot;
  package$repositories.not_i5pqgw$ = not;
  package$repositories.FilterExpAnd = FilterExpAnd;
  package$repositories.and_7otsba$ = and;
  package$repositories.FilterExpOr = FilterExpOr;
  package$repositories.or_7otsba$ = or;
  package$repositories.FieldUnop = FieldUnop;
  package$repositories.FieldIsNull = FieldIsNull;
  package$repositories.isNull_22ie0x$ = isNull;
  package$repositories.FieldBinop = FieldBinop;
  package$repositories.SimpleFieldBinop = SimpleFieldBinop;
  package$repositories.FieldEqs = FieldEqs;
  package$repositories.eq_7j7joa$ = eq;
  package$repositories.FieldLike = FieldLike;
  package$repositories.like_isrew5$ = like;
  package$repositories.FieldGt = FieldGt;
  package$repositories.gt_3v63qw$ = gt;
  package$repositories.FieldGte = FieldGte;
  package$repositories.gte_3v63qw$ = gte;
  package$repositories.FieldLt = FieldLt;
  package$repositories.lt_3v63qw$ = lt;
  package$repositories.FieldLte = FieldLte;
  package$repositories.lte_3v63qw$ = lte;
  package$repositories.FieldBinopOnSet = FieldBinopOnSet;
  package$repositories.FieldWithin = FieldWithin;
  package$repositories.FieldWithinComplex = FieldWithinComplex;
  $$importsForInline$$['kuick-core'] = _;
  package$repositories.ModelRepository = ModelRepository;
  package$repositories.updateBy_tr3q0e$ = updateBy;
  Object.defineProperty(package$repositories, 'PAGE_MAX_SIZE', {
    get: function () {
      return PAGE_MAX_SIZE;
    }
  });
  package$repositories.Resultset = Resultset;
  package$repositories.Page = Page;
  package$repositories.toList_bt9k8n$ = toList_0;
  package$repositories.ViewRepository = ViewRepository;
  package$repositories.ScoredModel = ScoredModel;
  package$repositories.ScoredViewRepository = ScoredViewRepository;
  var package$memory = package$repositories.memory || (package$repositories.memory = {});
  package$memory.ModelRepositoryMemory = ModelRepositoryMemory;
  var package$patterns = package$repositories.patterns || (package$repositories.patterns = {});
  package$patterns.AbstractViewRepositoryDecorator = AbstractViewRepositoryDecorator;
  package$patterns.BusModelRepositoryDecorator = BusModelRepositoryDecorator;
  package$patterns.CQRSModelRepositoryDecorator = CQRSModelRepositoryDecorator;
  package$patterns.Cache = Cache;
  package$patterns.CachedModelRepository = CachedModelRepository;
  package$patterns.FastEventuallyConsistentModelRepositoryDecorator = FastEventuallyConsistentModelRepositoryDecorator;
  Object.defineProperty(ModelChangeType, 'INSERT', {
    get: ModelChangeType$INSERT_getInstance
  });
  Object.defineProperty(ModelChangeType, 'UPDATE', {
    get: ModelChangeType$UPDATE_getInstance
  });
  Object.defineProperty(ModelChangeType, 'DELETE', {
    get: ModelChangeType$DELETE_getInstance
  });
  Object.defineProperty(ModelChangeType, 'INIT', {
    get: ModelChangeType$INIT_getInstance
  });
  package$patterns.ModelChangeType = ModelChangeType;
  package$patterns.changeEventTopic_twq1dk$ = changeEventTopic;
  package$patterns.publishInsert_jgqz93$ = publishInsert;
  package$patterns.publishUpdate_jgqz93$ = publishUpdate;
  package$patterns.ModelRepositoryDecorator = ModelRepositoryDecorator;
  package$patterns.NullModelRepository = NullModelRepository;
  package$patterns.ViewRepositoryDecorator = ViewRepositoryDecorator;
  package$patterns.ViewListeners = ViewListeners;
  package$patterns.Builder = Builder;
  package$patterns.Updater = Updater;
  ModelRepositoryMemory.prototype.insertMany_943sby$ = ModelRepository.prototype.insertMany_943sby$;
  ModelRepositoryMemory.prototype.updateMany_943sby$ = ModelRepository.prototype.updateMany_943sby$;
  BusModelRepositoryDecorator.prototype.insertMany_943sby$ = ModelRepository.prototype.insertMany_943sby$;
  BusModelRepositoryDecorator.prototype.updateMany_943sby$ = ModelRepository.prototype.updateMany_943sby$;
  CQRSModelRepositoryDecorator.prototype.insertMany_943sby$ = ModelRepository.prototype.insertMany_943sby$;
  CQRSModelRepositoryDecorator.prototype.updateMany_943sby$ = ModelRepository.prototype.updateMany_943sby$;
  ModelRepositoryDecorator.prototype.insertMany_943sby$ = ModelRepository.prototype.insertMany_943sby$;
  ModelRepositoryDecorator.prototype.updateMany_943sby$ = ModelRepository.prototype.updateMany_943sby$;
  NullModelRepository.prototype.insertMany_943sby$ = ModelRepository.prototype.insertMany_943sby$;
  NullModelRepository.prototype.updateMany_943sby$ = ModelRepository.prototype.updateMany_943sby$;
  K_LOCAL_DATE_FORMAT = 'yyyy-MM-dd';
  K_LOCAL_DATE_TIME_FORMAT = 'YYYY-MM-DDTHH:mm:ss';
  PAGE_MAX_SIZE = 1000;
  Kotlin.defineModule('kuick-core', _);
  return _;
}));

//# sourceMappingURL=kuick-core.js.map
