{
	"system_time": "2020-02-20T21:00:00Z",
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
					"|категории = Собаки",
					"|страница = Проект:Project 1/Новые статьи",
					"|элементов = 10",
					"|шапка = {{НОВОЕ НАЧАЛО БЛОКА}}\\n",
					"|подвал = \\n{{НОВЫЙ КОНЕЦ БЛОКА}}",
					"}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": [
				"{{НАЧАЛО БЛОКА}}",
				"* [[Гонконский штрудель]]",
				"* [[Тайский бегемот]]",
				"* [[Белый пудель]]",
				"* [[Розовый пудель]]",
				"* [[Странный пёс]]",
				"* [[Мегадог]]",
				"{{КОНЕЦ БЛОКА}}"
				]
            }
		],
		"exists": [
		    "Гонконский штрудель",
			"Мегадог",
			"Тайский бегемот",
			"Странный пёс"
		],
		"firstRevision": [
		    {
			"revid": 666, "timestamp": 1275804366000, "title": "Квазисобака", "summary": "create page Квазисобака",
			"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
			"revid": 680, "timestamp": 1275804400000, "title": "Бульдог", "summary": "create new fucking page",
			"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			}, {
			"revid": 685, "timestamp": 1275804500000, "title": "Плоскожопый долгонос", "summary": "create new page",
			"user" : "MegaUser 3", "minor": false, "bot": false, "rvnew": true, "size": 666
			}, {
			"revid": 686, "timestamp": 1275805366000, "title": "Серо-буро-малиновый пенчекряк", "summary": "create new page",
			"user" : "MegaUser 4", "minor": false, "bot": false, "rvnew": true, "size": 700
			}, {
			"revid": 687, "timestamp": 1275806000000, "title": "Немецкая овчарка", "summary": "create new page",
			"user" : "MegaUser 5", "minor": false, "bot": false, "rvnew": true, "size": 1000
			}, {
			"revid": 688, "timestamp": 1275807366000, "title": "Розовый пудель", "summary": "create new page",
			"user" : "MegaUser 6", "minor": false, "bot": false, "rvnew": true, "size": 1500
			}, {
			"revid": 690, "timestamp": 1275808366000, "title": "Белый пудель", "summary": "create new page",
			"user" : "MegaUser 7", "minor": false, "bot": false, "rvnew": true, "size": 50
		    }
		],
		"topRevision": [
			{
				"revid": 1000, "timestamp": 1275804366000, "title": "Проект:Project 1/Новые статьи", "summary": "Remove header",
				"user" : "SuperUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 680, "timestamp": 1275804400000, "title": "Бульдог", "summary": "create new fucking page",
				"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			} 
		],
		"pageHistory": [
		    {
				"title": "Проект:Project 1/Новые статьи/Параметры", "start_time": 1275804366000, "end_time": 1582232400000, "revisions": [
					{
						"revid": 1000, "timestamp": 1275804366000, "title": "Проект:Project 1/Новые статьи/Параметры", "summary": "Remove header",
						"user" : "SuperUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500, "previous": 990
					}
				]
			}
		],
		"revision": [
			{
				"revid": 990, "timestamp": 1275004166000, "title": "Проект:Project 1/Новые статьи/Параметры", "summary": "Add header",
				"user" : "SuperUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}
		],
		"revisionText": [
			{
				"revid": 990,
				"text": [
					"{{Участник:Bot template",
					"|тип = новые статьи",
					"|категории = Собаки",
					"|страница = Проект:Project 1/Новые статьи",
					"|элементов = 10",
					"|шапка = {{НАЧАЛО БЛОКА}}\\n",
					"|подвал = \\n{{КОНЕЦ БЛОКА}}",
					"}}"]
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
			    "{{НОВОЕ НАЧАЛО БЛОКА}}",
				"* [[Белый пудель]]",
				"* [[Розовый пудель]]",
				"* [[Немецкая овчарка]]",
				"* [[Серо-буро-малиновый пенчекряк]]",
				"* [[Плоскожопый долгонос]]",
				"* [[Бульдог]]",
				"* [[Квазисобака]]",
			    "* [[Гонконский штрудель]]",
		        "* [[Тайский бегемот]]",
				"* [[Странный пёс]]",
				"{{НОВЫЙ КОНЕЦ БЛОКА}}"
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
