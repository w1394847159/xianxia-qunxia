package com.xianxia.qunxia.data.repository

import com.xianxia.qunxia.game.Realm
import com.xianxia.qunxia.game.faction.DiplomaticRelation
import com.xianxia.qunxia.game.faction.DiplomaticType
import com.xianxia.qunxia.game.faction.Faction
import com.xianxia.qunxia.game.npc.NpcProfile

/**
 * 默认世界数据 —— 初次创建游戏时使用的初始 NPC 和宗门
 */
object DefaultWorldData {

    fun createDefaultNpcs(): Map<String, NpcProfile> {
        val npcs = mutableMapOf<String, NpcProfile>()

        // ========== 正道五大宗门 ==========

        // 青云门
        npcs["qingyun_zhangmen"] = NpcProfile(
            id = "qingyun_zhangmen", name = "玄天真人",
            title = "青云门掌门", gender = "男", age = 320, realm = Realm.DUJIE,
            faction = "qingyun", location = "青云山",
            personality = "仙风道骨，心怀天下，为人正直但不迂腐，对晚辈宽厚但要求严格",
            appearance = "白发如雪，面容却如中年，身着青白道袍，手持拂尘",
            background = "三百岁渡劫期的绝世高人，青云门第十三代掌门，正道领袖",
            goals = listOf("培养下一代接班人", "维持正道联盟稳固", "寻找突破渡劫的机缘"),
            quirks = listOf("每月十五必在望月崖独坐", "嗜好弈棋但棋艺极差"),
            skills = listOf("青云剑诀", "五雷正法", "天罡北斗阵"),
            isCoreAgent = true, decisionFrequency = 1
        )
        npcs["qingyun_dizi"] = NpcProfile(
            id = "qingyun_dizi", name = "陆清瑶",
            title = "青云门首徒", gender = "女", age = 28, realm = Realm.JINDAN,
            faction = "qingyun", location = "青云山",
            personality = "清冷如霜，外冷内热，天赋异禀但性子孤傲",
            appearance = "一袭白衣，青丝如瀑，面若寒霜却眉目如画",
            background = "玄天真人的关门弟子，年仅二十八便已金丹期，被誉为青云门千年不遇的天才",
            goals = listOf("突破元婴期", "下山历练增长见闻"),
            quirks = listOf("不喜欢与人肢体接触", "养了一只白狐"),
            skills = listOf("青云剑诀", "寒冰诀", "御剑术"),
            isCoreAgent = true, decisionFrequency = 2
        )
        npcs["qingyun_changlao"] = NpcProfile(
            id = "qingyun_changlao", name = "云麓长老",
            title = "青云门传功长老", gender = "男", age = 450, realm = Realm.HUASHEN,
            faction = "qingyun", location = "青云山",
            personality = "古板严肃，讲究规矩，最重门规礼法",
            appearance = "枯瘦老者，眉毛花白，手持一根墨玉杖",
            background = "青云门辈分最高的长老，掌管藏经阁和弟子考核",
            goals = listOf("维护门规威严", "找到合适的衣钵传人"),
            quirks = listOf("说话必引经据典", "讨厌一切新奇事物"),
            skills = listOf("万卷道藏", "禁制阵法", "炼丹术"),
            isCoreAgent = false
        )

        // 天剑宗
        npcs["tianjian_zhangmen"] = NpcProfile(
            id = "tianjian_zhangmen", name = "剑无名",
            title = "天剑宗宗主", gender = "男", age = 260, realm = Realm.DUJIE,
            faction = "tianjian", location = "天剑峰",
            personality = "锋芒毕露，狂放不羁，一生痴于剑道，不谙世事",
            appearance = "黑衣黑发，面容冷峻，背上七把古剑，周身剑气缭绕",
            background = "剑道奇才，七岁学剑，十五岁筑基，百岁化神，二百六十岁已达渡劫期",
            goals = listOf("创出第八式剑法", "与天下剑道高手切磋"),
            quirks = listOf("吃饭睡觉都抱着剑", "从不跟不用剑的人说话"),
            skills = listOf("无极剑道", "七绝剑阵", "剑意化形"),
            isCoreAgent = true, decisionFrequency = 1
        )
        npcs["tianjian_dizi"] = NpcProfile(
            id = "tianjian_dizi", name = "谢云流",
            title = "天剑宗大弟子", gender = "男", age = 35, realm = Realm.YUANYING,
            faction = "tianjian", location = "天剑峰",
            personality = "沉默寡言，重情重义，是那种'答应你的事死也会做到'的人",
            appearance = "身材修长，面容刚毅，左眉一道剑疤",
            background = "孤儿出身，被剑无名捡回山门，对天剑宗忠心耿耿",
            goals = listOf("守护天剑宗", "找到失散多年的妹妹"),
            quirks = listOf("紧张时会不自觉摸剑柄", "滴酒不沾"),
            skills = listOf("天剑诀", "御剑飞行"),
            isCoreAgent = true, decisionFrequency = 2
        )

        // 慈航静斋
        npcs["cihang_zhai_zhu"] = NpcProfile(
            id = "cihang_zhai_zhu", name = "妙音师太",
            title = "慈航静斋斋主", gender = "女", age = 380, realm = Realm.HUASHEN,
            faction = "cihang", location = "慈航山",
            personality = "慈悲为怀，智慧通达，看透世事却不冷漠",
            appearance = "老尼打扮，面容慈祥，手持一串碧玉念珠，双目温润如玉",
            background = "慈航静斋第二百三十代斋主，佛法精深，医术通天",
            goals = listOf("渡化世人", "寻找失传的慈航真经下半部"),
            quirks = listOf("每餐只食一粥一菜", "从不杀生，连蚂蚁都绕道走"),
            skills = listOf("慈航普度诀", "大还丹手", "梵音渡厄"),
            isCoreAgent = true, decisionFrequency = 1
        )

        // 万宝楼
        npcs["wanbao_lou_zhu"] = NpcProfile(
            id = "wanbao_lou_zhu", name = "金满堂",
            title = "万宝楼楼主", gender = "男", age = 180, realm = Realm.YUANYING,
            faction = "wanbao", location = "天机城",
            personality = "精明圆滑，笑面虎，重利但不失大义，信奉'和气生财'",
            appearance = "一身锦袍，白白胖胖，手指上戴满储物戒指，永远笑眯眯",
            background = "白手起家的商业奇才，万宝楼遍布天下，消息灵通程度堪比天机阁",
            goals = listOf("垄断修仙界的灵石流通", "找到传说中的聚宝盆"),
            quirks = listOf("算账时必用算盘不用神识", "收藏了三千六百把不同的茶壶"),
            skills = listOf("鉴定术（万物皆可鉴）", "谈判术", "阵法机关术"),
            isCoreAgent = true, decisionFrequency = 2
        )

        // 星辰阁
        npcs["xingchen_ge_zhu"] = NpcProfile(
            id = "xingchen_ge_zhu", name = "星月仙子",
            title = "星辰阁阁主", gender = "女", age = 290, realm = Realm.HUASHEN,
            faction = "xingchen", location = "观星台",
            personality = "神秘莫测，神神叨叨，说话只说三分，但句句意有所指",
            appearance = "紫衣华服，面戴轻纱，眸若星辰，周身星辉流转",
            background = "星辰阁世代观测天机，预言大事，星月仙子是百年来最具天赋的观星者",
            goals = listOf("阻止预言中的'天倾之劫'", "找到上古星图"),
            quirks = listOf("从不在白天出门", "喜欢自言自语跟星星说话"),
            skills = listOf("星象推演术", "因果推算", "星辰阵法"),
            isCoreAgent = true, decisionFrequency = 1
        )

        // ========== 邪道四门 ==========

        // 血影教
        npcs["xueying_jiao_zhu"] = NpcProfile(
            id = "xueying_jiao_zhu", name = "血煞老祖",
            title = "血影教教主", gender = "男", age = 500, realm = Realm.DUJIE,
            faction = "xueying", location = "血影深渊",
            personality = "残忍嗜杀，喜怒无常，但极其护短——本教的人只有他能杀",
            appearance = "血发红瞳，面容枯槁如骷髅，一身血色长袍，周身百鬼哭嚎",
            background = "五百年的老魔头，靠血祭之法硬生生堆到渡劫期，正道人人欲诛之",
            goals = listOf("以血证道，突破飞升", "吞噬其他邪派统一魔道"),
            quirks = listOf("每天要饮三碗鲜血", "养了一条千年血蛟"),
            skills = listOf("血海大法", "噬魂术", "血影分身"),
            isCoreAgent = true, decisionFrequency = 1
        )
        npcs["xueying_hufa"] = NpcProfile(
            id = "xueying_hufa", name = "柳血衣",
            title = "血影教左护法", gender = "女", age = 45, realm = Realm.YUANYING,
            faction = "xueying", location = "血影深渊",
            personality = "冷血无情，杀人如麻，但对血煞老祖忠心耿耿",
            appearance = "一袭红衣似血，赤足，脚踝系银铃，容貌妖艳至极",
            background = "幼年被血煞老祖所救，从此誓死追随，是血影教最锋利的刀",
            goals = listOf("为教主扫清一切障碍"),
            quirks = listOf("杀人前会笑", "从不穿鞋"),
            skills = listOf("血影刺杀术", "魅惑之术", "毒术"),
            isCoreAgent = true, decisionFrequency = 2
        )

        // 合欢宗
        npcs["hehuan_zong_zhu"] = NpcProfile(
            id = "hehuan_zong_zhu", name = "花弄影",
            title = "合欢宗宗主", gender = "女", age = 200, realm = Realm.HUASHEN,
            faction = "hehuan", location = "极乐谷",
            personality = "妩媚风流，游戏人间，看似放荡实则心机深沉",
            appearance = "身材曼妙，薄纱蔽体，面若桃花，一颦一笑皆有万种风情",
            background = "合欢宗三百年来最出色的宗主，采补之术出神入化，但没人知道她的真实来历",
            goals = listOf("突破渡劫期", "找到真心人（虽然她自己都不信）"),
            quirks = listOf("身边永远跟着四个女弟子", "收集天下美男子的画像"),
            skills = listOf("合欢秘术", "幻术", "媚术"),
            isCoreAgent = true, decisionFrequency = 1
        )

        // 鬼王宗
        npcs["guiwang"] = NpcProfile(
            id = "guiwang", name = "鬼王",
            title = "鬼王宗宗主", gender = "男", age = 600, realm = Realm.DUJIE,
            faction = "guiwang", location = "幽冥谷",
            personality = "阴沉诡谲，城府极深，不出手则已，一出手必是绝杀",
            appearance = "黑袍遮面，只露出一双幽绿的眼睛，周身阴气森森",
            background = "六百年前是一介散修，得了上古鬼修传承后创立鬼王宗，行事低调但实力恐怖",
            goals = listOf("打开幽冥界通道", "炼制万鬼幡"),
            quirks = listOf("从不在阳光下出现", "说话声音像从地底传来"),
            skills = listOf("幽冥鬼道", "御鬼术", "尸傀炼制"),
            isCoreAgent = true, decisionFrequency = 2
        )

        // 散修高人
        npcs["sanxiu_jianxian"] = NpcProfile(
            id = "sanxiu_jianxian", name = "酒剑仙",
            title = "四海散人", gender = "男", age = 340, realm = Realm.DUJIE,
            faction = null, location = "未知",
            personality = "放浪形骸，玩世不恭，表面醉醺醺实则万事通透",
            appearance = "破旧道袍，腰悬酒葫芦，须发凌乱，眼神却清澈如少年",
            background = "三百年前突然出现的绝世高手，没人知道他的来历，一人一剑挑过七大宗门，打完继续喝酒",
            goals = listOf("喝遍天下美酒", "找个人传承剑道"),
            quirks = listOf("从不重复走同一条路", "喝酒必吟诗，吟完必忘"),
            skills = listOf("醉仙剑诀", "缩地成寸", "万剑归宗"),
            isCoreAgent = true, decisionFrequency = 1
        )

        // 天机阁（中立情报组织）
        npcs["tianji_laoren"] = NpcProfile(
            id = "tianji_laoren", name = "天机老人",
            title = "天机阁阁主", gender = "男", age = 800, realm = Realm.DACHENG,
            faction = "tianji", location = "天机阁",
            personality = "超然物外，洞悉一切但不插手，像在看一盘下了八百年的棋",
            appearance = "布衣老者，面容平凡，唯有双目深邃如星空，仿佛看穿了过去未来",
            background = "没人知道天机老人活了多少岁，只知道天机阁存在了多久，他就存在了多久",
            goals = listOf("保持天机阁绝对中立", "记录天下大事"),
            quirks = listOf("从不离开天机阁", "知道所有事但从不说破"),
            skills = listOf("天机推演", "过目不忘", "万象森罗"),
            isCoreAgent = true, decisionFrequency = 1
        )

        // ========== 更多核心NPC ==========

        npcs["yaowang"] = NpcProfile(
            id = "yaowang", name = "药老人",
            title = "隐世丹师", gender = "男", age = 400, realm = Realm.HUASHEN,
            faction = null, location = "药王谷",
            personality = "痴迷炼丹，不通人情世故，只要你拿稀有药材来什么都好说",
            appearance = "一身药渍的青衫，头发乱糟糟，十指因常年炼丹呈墨绿色",
            background = "修仙界第一丹师，炼出的九转金丹能让人直接突破一个大境界，但性格古怪",
            goals = listOf("炼制传说中的'大道金丹'", "收集上古丹方"),
            quirks = listOf("拿丹药当糖豆吃", "把药材按'可爱程度'分类"),
            skills = listOf("九转炼丹术", "百草辨识", "药阵"),
            isCoreAgent = true, decisionFrequency = 2
        )

        npcs["miaojiang_nv"] = NpcProfile(
            id = "miaojiang_nv", name = "蓝凤凰",
            title = "苗疆圣女", gender = "女", age = 22, realm = Realm.JINDAN,
            faction = null, location = "苗疆十万大山",
            personality = "古灵精怪，天真烂漫，但用蛊的时候会露出恶魔般的笑容",
            appearance = "苗族银饰盛装，赤足，手腕脚腕戴满银铃，笑起来有两个酒窝",
            background = "苗疆百年一遇的蛊术天才，三岁养本命蛊，十岁蛊术大成，如今已是金丹期",
            goals = listOf("找到传说中的'万蛊母虫'", "去中原看看（家里不让）"),
            quirks = listOf("身上永远有三只以上蛊虫", "喜欢用蛊虫恶作剧"),
            skills = listOf("御蛊术", "巫术", "毒术"),
            isCoreAgent = true, decisionFrequency = 2
        )

        // 补充更多宗门NPC...
        npcs["wenren_yue"] = NpcProfile(
            id = "wenren_yue", name = "温如玉",
            title = "万象书院山长", gender = "男", age = 55, realm = Realm.JINDAN,
            faction = "wanxiang", location = "万象书院",
            personality = "温文尔雅，满腹经纶，真正的君子如玉",
            appearance = "青衫儒冠，面容清雅，手持一卷竹简",
            background = "科举出身却弃儒修仙，创办万象书院主张'以文入道'",
            goals = listOf("培养更多'以文入道'的弟子", "搜集天下失传典籍"),
            quirks = listOf("吟诗时必摇头晃脑", "藏书八万卷每一本都读过"),
            skills = listOf("浩然正气诀", "诗词杀阵", "教化之术"),
            isCoreAgent = true, decisionFrequency = 2
        )

        return npcs
    }

    fun createDefaultFactions(): Map<String, Faction> {
        val factions = mutableMapOf<String, Faction>()

        factions["qingyun"] = Faction(
            id = "qingyun", name = "青云门",
            description = "正道第一大派，以剑修和雷法闻名，位于青云山",
            realm = "青云山", leaderId = "qingyun_zhangmen",
            elders = listOf("qingyun_changlao"),
            disciples = listOf("qingyun_dizi"),
            influence = 950, wealth = 800, military = 900, reputation = 980,
            tags = listOf("正道", "剑修", "雷法", "名门正派")
        )

        factions["tianjian"] = Faction(
            id = "tianjian", name = "天剑宗",
            description = "修剑狂人的聚集地，只收剑道天才，位于天剑峰",
            realm = "天剑峰", leaderId = "tianjian_zhangmen",
            disciples = listOf("tianjian_dizi"),
            influence = 850, wealth = 500, military = 950, reputation = 880,
            tags = listOf("正道", "剑修", "武痴")
        )

        factions["cihang"] = Faction(
            id = "cihang", name = "慈航静斋",
            description = "佛修圣地，以慈悲济世为宗旨，医术冠绝天下",
            realm = "慈航山", leaderId = "cihang_zhai_zhu",
            influence = 800, wealth = 600, military = 300, reputation = 990,
            tags = listOf("正道", "佛修", "医修", "中立")
        )

        factions["wanbao"] = Faction(
            id = "wanbao", name = "万宝楼",
            description = "天下第一商号，灵石流通的掌控者，消息灵通",
            realm = "天机城", leaderId = "wanbao_lou_zhu",
            influence = 700, wealth = 1000, military = 200, reputation = 650,
            tags = listOf("中立", "商道", "情报")
        )

        factions["xingchen"] = Faction(
            id = "xingchen", name = "星辰阁",
            description = "观天象、测天机的神秘组织，不问世事只管'看'",
            realm = "观星台", leaderId = "xingchen_ge_zhu",
            influence = 600, wealth = 400, military = 100, reputation = 850,
            tags = listOf("中立", "天机", "神秘")
        )

        factions["xueying"] = Faction(
            id = "xueying", name = "血影教",
            description = "邪道第一势力，以血炼之法修炼，行事残忍",
            realm = "血影深渊", leaderId = "xueying_jiao_zhu",
            elders = listOf(), disciples = listOf("xueying_hufa"),
            influence = 900, wealth = 700, military = 950, reputation = 50,
            tags = listOf("邪道", "血修", "魔教")
        )

        factions["hehuan"] = Faction(
            id = "hehuan", name = "合欢宗",
            description = "以双修采补之术闻名，行事风流不羁",
            realm = "极乐谷", leaderId = "hehuan_zong_zhu",
            influence = 650, wealth = 850, military = 400, reputation = 200,
            tags = listOf("邪道", "双修", "幻术")
        )

        factions["guiwang"] = Faction(
            id = "guiwang", name = "鬼王宗",
            description = "御鬼控尸的隐秘宗门，常年盘踞幽冥谷",
            realm = "幽冥谷", leaderId = "guiwang",
            influence = 750, wealth = 500, military = 850, reputation = 100,
            tags = listOf("邪道", "鬼修", "尸道")
        )

        factions["wanxiang"] = Faction(
            id = "wanxiang", name = "万象书院",
            description = "以文入道、儒道双修的书院，主张'读书即修行'",
            realm = "万象城", leaderId = "wenren_yue",
            influence = 350, wealth = 300, military = 150, reputation = 750,
            tags = listOf("中立", "儒修", "书院")
        )

        factions["tianji"] = Faction(
            id = "tianji", name = "天机阁",
            description = "超然中立的情报组织，知晓天下事但从不插手",
            realm = "天机阁", leaderId = "tianji_laoren",
            influence = 400, wealth = 900, military = 50, reputation = 700,
            tags = listOf("中立", "情报", "超然")
        )

        return factions
    }

    fun initializeDiplomacy(factions: MutableMap<String, Faction>) {
        // 正道同盟
        val zhengdao = listOf("qingyun", "tianjian", "cihang")
        for (f in zhengdao) {
            for (g in zhengdao) {
                if (f != g) {
                    factions[f]?.relations?.set(g, DiplomaticRelation(g, DiplomaticType.ALLY, 80))
                }
            }
        }

        // 邪道关系
        val xiedao = listOf("xueying", "hehuan", "guiwang")
        for (f in xiedao) {
            for (g in xiedao) {
                if (f != g) {
                    factions[f]?.relations?.set(g, DiplomaticRelation(g, DiplomaticType.NEUTRAL, 10))
                }
            }
        }

        // 正道 vs 邪道
        for (zd in zhengdao) {
            for (xd in xiedao) {
                factions[zd]?.relations?.set(xd, DiplomaticRelation(xd, DiplomaticType.HOSTILE, -60))
                factions[xd]?.relations?.set(zd, DiplomaticRelation(zd, DiplomaticType.HOSTILE, -60))
            }
        }

        // 中立势力对谁都友好
        val zhongli = listOf("wanbao", "xingchen", "wanxiang", "tianji")
        for (zl in zhongli) {
            for (all in factions.keys) {
                if (zl != all && factions[all]?.relations?.containsKey(zl) != true) {
                    factions[zl]?.relations?.set(all, DiplomaticRelation(all, DiplomaticType.FRIENDLY, 30))
                }
            }
        }
    }
}
