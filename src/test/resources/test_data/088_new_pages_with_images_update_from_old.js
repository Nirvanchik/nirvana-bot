{
	"wiki" : {
		"whatTranscludesHere": [
			{ "title": "Участник:Bot template", "list": "Проект:Project 1/Новые статьи/Параметры" }
		],
		"namespaceIdentifier": [
			{ "number": 0, "id": "" },
			{ "number": 2, "id": "Участник" },
			{ "number": 10, "id": "Шаблон" },
			{ "number": 6, "id": "Файл" }
		],
		"pageText": [
		    {
				"title": "Файл:Lanzhousaurus BW.jpg",
				"text": []
			},
			{
				"title": "Файл:CaecilianNHM edited.PNG",
				"text": []
			},
			{
				"title": "Проект:Project 1/Новые статьи/Параметры",
				"text": [
					"{{Участник:Bot template",
					"|тип = новые статьи с изображениями",
					"|категории = Герпетология",
					"|страница = Проект:Project 1/Новые статьи",
					"|элементов = 10",
					"|часов = 8760",
					"|формат элемента = Файл:%(имя файла)|[[%(название)]]",
					"|шапка           = <div align=\"center\"><gallery perrow=\"3\" widths=\"125px\" heights=\"125px\" caption=\"Герпетология\">\\n",
					"|подвал          = \\n</gallery>",
					"}}"]
			},
			{
				"title": "просто статья",
				"text": [
				"Начало статьи.",
				"Середина статьи.",
				"",
				"Конец статьи."
				]
			},
			{
				"title": "пустая статья",
				"text": [""]
			},
			{
				"title": "Lanzhousaurus",
				"text": [
				"{{Таксон",
				"| image file  = Lanzhousaurus BW.jpg",
				"| image descr = Реконструкция",
				"| regnum      = Животные",
				"| parent      = Styracosterna",
				"| rang        = Род",
				"| latin       = Lanzhousaurus",
				"| author      = You et al., 2005",
				"| children name = [[Монотипия (биологическая систематика)|Единственный]] [[Биологический вид|вид]]",
				"| children =",
				"<center>{{Вымер}} {{btname|Lanzhousaurus magnidens|<br>You et al., 2005}}</center>",
				"| Вымер       = Баррем",
				"| wikispecies = Lanzhousaurus",
				"}}",
				"",
				"'''''Lanzhousaurus'''''{{ref-la}} — род травоядных [[Орнитоподы|орнитоподовых]] [[Динозавры|динозавров]] из клады [[Styracosterna]] из [[меловой период|мела]] [[Азия|Азии]]."
				]
			},
			{
				"title": "Tacit Fury",
				"text": [""]
			},
			{
				"title": "Цейлонский рыбозмей",
				"text": ["Начало.", "какой-то текст [[Файл:CaecilianNHM edited.PNG|left|mini|100px]] текст", "Конец."]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": [
					"<div align=\"center\"><gallery perrow=\"3\" widths=\"125px\" heights=\"125px\" caption=\"Герпетология\">",
					"File:SL Bundala NP asv2020-01 img30.jpg|[[Цейлонский макак]]",
					"</gallery>"
				],
            },
            {
                "title": "MULTIVERSE",
                "text": null
            }
		],
		"firstRevision": [
			{
				"revid": 666, "timestamp": 1275804366000, "title": "просто статья", "summary": "create page Квазисобака",
				"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 667, "timestamp": 1275804376000, "title": "пустая статья", "summary": "create page просто статья",
				"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 680, "timestamp": 1275804400000, "title": "Lanzhousaurus", "summary": "create new fucking page",
				"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			}, {
				"revid": 685, "timestamp": 1275804500000, "title": "Цейлонский рыбозмей", "summary": "create new page",
				"user" : "MegaUser 3", "minor": false, "bot": false, "rvnew": true, "size": 666
			}, {
				"revid": 686, "timestamp": 1275805366000, "title": "MULTIVERSE", "summary": "create new page",
				"user" : "MegaUser 4", "minor": false, "bot": false, "rvnew": true, "size": 700
			}
		],
		"topRevision": [
			{
				"title": "Проект:Project 1/Новые статьи"
			} 
		],
		"templates": [
		],
		"exists": [
			"File:Lanzhousaurus BW.jpg",
			"File:CaecilianNHM edited.PNG"
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	просто_статья	4350785		35169	20160625015051",
		"1	пустая_статья	4350786		5169	20160624015051",
		"3	Lanzhousaurus	6149797		6559	20160623100936",
		"4	Цейлонский_рыбозмей	6082689		12205	20160623100936",
		"5	MULTIVERSE	6003741		6518	20160624224342",]
	],
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи",
			"text": [
					"<div align=\"center\"><gallery perrow=\"3\" widths=\"125px\" heights=\"125px\" caption=\"Герпетология\">",
					"Файл:Lanzhousaurus BW.jpg|[[Lanzhousaurus]]",
					"Файл:CaecilianNHM edited.PNG|[[Цейлонский рыбозмей]]",
					"File:SL Bundala NP asv2020-01 img30.jpg|[[Цейлонский макак]]",
					"</gallery>"
				],
			"section": -2
		}
	],
	"expected_tools_queries": [
		{
			"contains": ["Герпетология"]
		}
	]
}