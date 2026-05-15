# Changelog

## 1.0.0 (2026-05-15)


### Features

* add mod compatibility database for automatic conflict resolution ([7077c38](https://github.com/SharkMI-0x7E/CardBoard/commit/7077c38f0386c3df7980c07f6a84ef06c8f70b3e))
* **phase3-step1:** add mixin conflict detection data models ([d272171](https://github.com/SharkMI-0x7E/CardBoard/commit/d272171d6aa298837daf7301eb87dff97ab2d101))
* **phase3-step2:** add mixin config scanner for *.mixins.json files ([1521208](https://github.com/SharkMI-0x7E/CardBoard/commit/152120826f9b4451773df9a7f3aaef7758555b49))
* **phase3-step3:** add ASM bytecode annotation scanner for mixin analysis ([5f378ed](https://github.com/SharkMI-0x7E/CardBoard/commit/5f378ed146db7de710050a46ab3e0efbd4f1ab0d))
* **phase3-step4:** add mapping bridge for intermediary to Mojang name conversion ([02e7e62](https://github.com/SharkMI-0x7E/CardBoard/commit/02e7e62a060ae202255627739eb02da153e69d84))
* **phase3-step5:** add mixin conflict detection algorithm with R1-R6 rules ([3b66d09](https://github.com/SharkMI-0x7E/CardBoard/commit/3b66d0907cb7f4b97c2166fedd1e1d3aa40a710f))
* **phase3-step6:** add conflict report generator with console and JSON output ([be4ea24](https://github.com/SharkMI-0x7E/CardBoard/commit/be4ea24a55a671f30f85992d92b97fd9618ec03a))
* **phase3-step7:** add mixin conflict detection config options ([9f280a9](https://github.com/SharkMI-0x7E/CardBoard/commit/9f280a93a1add73aba9d2ab2d0f57bc9f5f83373))
* **phase3-step8:** integrate conflict scan into CardboardMixinPlugin lifecycle ([3cb264d](https://github.com/SharkMI-0x7E/CardBoard/commit/3cb264d36f0f0f381e8b3f4c85614557c2f83905))


### Bug Fixes

* motdChanged 判断增加 server.getMotd() 比较，防止覆盖 MiniMOTD 修改 ([2c56e85](https://github.com/SharkMI-0x7E/CardBoard/commit/2c56e85a082ad43321e1e6f055ffa39b82af2dd4))
* override Cardboard MOTD logic to let MiniMOTD work ([1ba34c3](https://github.com/SharkMI-0x7E/CardBoard/commit/1ba34c300a7d06cec787bacfa403ac59e78ac3da))
* 修复缺少 method 参数导致 InvalidInjectionException ([c300dbc](https://github.com/SharkMI-0x7E/CardBoard/commit/c300dbcfd21c274a2b180821e60cd10f93e96ba2))
