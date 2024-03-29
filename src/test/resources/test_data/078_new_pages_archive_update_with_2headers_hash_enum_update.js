{
	"wiki" : {
		"whatTranscludesHere": [
			{ "title": "Участник:Bot template", "list": "Проект:Project 1/Новые статьи/Параметры" }
		],
		"namespaceIdentifier": [
			{ "number": 0, "id": "" },
			{ "number": 2, "id": "Участник" }
		],
		"pageText": [
			{
				"title": "Проект:Project 1/Новые статьи/Параметры",
				"text": [
					"{{Участник:Bot template",
					"|тип = новые статьи",
					"|категория = Собаки",
					"|страница = Проект:Project 1/Новые статьи",
					"|архив = Проект:Project 1/Новые статьи/Архив",
					"|параметры архива = нумерация решетками",
					"|формат заголовка в архиве = == %(год) ==",
					"|формат подзаголовка в архиве = === %(месяц) ===",
					"|элементов = 6",
					"}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": [
				"* [[Гонконский штрудель]]",
				"* [[Тайский бегемот]]",
				"* [[Белый пудель]]",
				"* [[Розовый пудель]]",
				"* [[Странный пёс]]",
				"* [[Мегадог]]" ]
            },
            {
                "title": "Проект:Project 1/Новые статьи/Архив",
                "text": [
				"== 2020 ==",
				"=== январь ===",
				"# [[Старая собака]]",
				]
            }
		],
		"firstRevision": [
			{
				"revid": 666, "timestamp": "2020-02-14T20:48:40Z", "title": "Квазисобака", "summary": "create page Квазисобака",
				"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 680, "timestamp": "2020-02-14T20:38:40Z", "title": "Бульдог", "summary": "create new fucking page",
				"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			}, {
				"revid": 685, "timestamp": "2020-02-14T20:30:40Z", "title": "Плоскожопый долгонос", "summary": "create new page",
				"user" : "MegaUser 3", "minor": false, "bot": false, "rvnew": true, "size": 666
			}, {
				"revid": 686, "timestamp": "2020-02-14T20:25:40Z", "title": "Серо-буро-малиновый пенчекряк", "summary": "create new page",
				"user" : "MegaUser 4", "minor": false, "bot": false, "rvnew": true, "size": 700
			}, {
				"revid": 687, "timestamp": "2020-02-14T20:20:40Z", "title": "Немецкая овчарка", "summary": "create new page",
				"user" : "MegaUser 5", "minor": false, "bot": false, "rvnew": true, "size": 1000
			}, {
				"revid": 688, "timestamp": "2020-02-14T20:19:40Z", "title": "Розовый пудель", "summary": "create new page",
				"user" : "MegaUser 6", "minor": false, "bot": false, "rvnew": true, "size": 1500
			}, {
				"revid": 690, "timestamp": "2020-02-14T20:18:40Z", "title": "Белый пудель", "summary": "create new page",
				"user" : "MegaUser 7", "minor": false, "bot": false, "rvnew": true, "size": 50
			}, {
				"revid": 690, "timestamp": "2020-01-24T19:20:40Z", "title": "Мегадог", "summary": "create new page",
				"user" : "MegaUser 8", "minor": false, "bot": false, "rvnew": true, "size": 50
			}, {
				"revid": 690, "timestamp": "2020-02-14T19:19:40Z", "title": "Странный пёс", "summary": "create new page",
				"user" : "MegaUser 8", "minor": false, "bot": false, "rvnew": true, "size": 50
			}, {
				"revid": 690, "timestamp": "2020-02-14T19:18:40Z", "title": "Тайский бегемот", "summary": "create new page",
				"user" : "MegaUser 8", "minor": false, "bot": false, "rvnew": true, "size": 50
			}
		],
		"topRevision": [
			{
				"title": "Проект:Project 1/Новые статьи"
			} 
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	Белый_пудель	690		25460	20160603012302",
		"2	Розовый_пудель	688		18924	20160603134155",
		"3	Немецкая_овчарка	687		12148	20160603134155",
		"4	Серо-буро-малиновый_пенчекряк	686		23643	20160531174602",
		"5	Плоскожопый_долгонос	685		1904	20160502235041",
		"6	Бульдог	680		45204	20160603134155",
		"7	Квазисобака	666		26808	20160603134155" ]
	],
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи",
			"text": [
				"* [[Белый пудель]]",
				"* [[Розовый пудель]]",
				"* [[Немецкая овчарка]]",
				"* [[Серо-буро-малиновый пенчекряк]]",
				"* [[Плоскожопый долгонос]]",
				"* [[Бульдог]]" ],
			"section": -2
		},
		{
			"title": "Проект:Project 1/Новые статьи/Архив",
			"text": [
			    "== 2020 ==",
				"=== февраль ===",
				"# [[Гонконский штрудель]]",
				"# [[Тайский бегемот]]",
				"# [[Странный пёс]]",
				"=== январь ===",
				"# [[Мегадог]]",
				"# [[Старая собака]]",
				""
			],
			"section": -2
		}
	],
	"expected_tools_queries": [
		{
			"contains": ["Собаки"]
		}
	]
}