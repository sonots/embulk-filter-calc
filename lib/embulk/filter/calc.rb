Embulk::JavaPlugin.register_filter(
  "calc", "org.embulk.filter.calc.CalcFilterPlugin",
  File.expand_path('../../../../classpath', __FILE__))
