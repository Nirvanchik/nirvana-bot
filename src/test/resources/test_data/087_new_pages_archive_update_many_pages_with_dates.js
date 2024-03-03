{
	"system_time": "2020-12-20T21:00:00Z",
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
					"|архив = Проект:Project 1/Новые статьи/Архив/%(год)",
					"|элементов = 6",
					"|формат элемента = {{Новая статья|%(название)|%(дата)|%(автор)}}",
					"}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": [
				"* {{Новая статья|Гонконский штрудель|2020-07-14T01:37:36Z|Lemmy Indarkness}}",
				"* {{Новая статья|Тайский бегемот|2019-12-24T19:18:40Z|MegaUser 8}}",
				"* {{Новая статья|Белый пудель|2020-02-14T20:18:40Z|MegaUser 7}}",
				"* {{Новая статья|Розовый пудель|2020-02-14T20:19:40Z|MegaUser 6}}",
				"* {{Новая статья|Странный пёс|2019-12-25T19:19:40Z|MegaUser 8}}",
				"* {{Новая статья|Мегадог|2020-01-14T19:20:40Z|MegaUser 8}}" ]
            },
            {
                "title": "Проект:Project 1/Новые статьи/Архив/2019",
                "text": [
				"* [[Старая собака]]"]
            },
            {
                "title": "Проект:Project 1/Новые статьи/Архив/2020",
                "text": null
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
				"{{Новая статья|Белый пудель|2020-02-14T20:18:40Z|MegaUser 7}}",
				"{{Новая статья|Розовый пудель|2020-02-14T20:19:40Z|MegaUser 6}}",
				"{{Новая статья|Немецкая овчарка|2020-02-14T20:20:40Z|MegaUser 5}}",
				"{{Новая статья|Серо-буро-малиновый пенчекряк|2020-02-14T20:25:40Z|MegaUser 4}}",
				"{{Новая статья|Плоскожопый долгонос|2020-02-14T20:30:40Z|MegaUser 3}}",
				"{{Новая статья|Бульдог|2020-02-14T20:38:40Z|MegaUser 2}}"],
			"section": -2
		},
		{
			"title": "Проект:Project 1/Новые статьи/Архив/2019",
			"text": [
				"* {{Новая статья|Тайский бегемот|2019-12-24T19:18:40Z|MegaUser 8}}",
				"* {{Новая статья|Странный пёс|2019-12-25T19:19:40Z|MegaUser 8}}",
                "* [[Старая собака]]"
			],
			"section": -2
		},
		{
			"title": "Проект:Project 1/Новые статьи/Архив/2020",
			"text": [
				"* {{Новая статья|Гонконский штрудель|2020-07-14T01:37:36Z|Lemmy Indarkness}}",
                "* {{Новая статья|Мегадог|2020-01-14T19:20:40Z|MegaUser 8}}",
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