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
				"title": "Проект:Project 1/Новые статьи/Параметры",
				"text": [
					"{{Участник:Bot template",
					"|тип = новые статьи с изображениями в карточке",
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
				"title": "Lanzhousaurus",
				"text": [
				"{{Таксон",
				"| image file  = {{часть изображения|изобр=Lanzhousaurus BW.jpg|позиция=center |подпись=|ширина=250|общая=450|верх=0|право=30|низ=20|лево=38|рамка=нет|помехи=да|увеличить=10}}",
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
				"'''''Lanzhousaurus'''''{{ref-la}} — род травоядных [[Орнитоподы|орнитоподовых]] [[Динозавры|динозавров]] из клады [[Styracosterna]] из [[меловой период|мела]] [[Азия|Азии]]."
				]
			},
			{
				"title": "Tacit Fury",
				"text": [""]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": null
            }
		],
		"firstRevision": [
			{
				"revid": 666, "timestamp": 1275804366000, "title": "просто статья", "summary": "create page Квазисобака",
				"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 680, "timestamp": 1275804400000, "title": "Lanzhousaurus", "summary": "create new fucking page",
				"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			}, {
				"revid": 685, "timestamp": 1275804500000, "title": "Tacit Fury", "summary": "create new page",
				"user" : "MegaUser 3", "minor": false, "bot": false, "rvnew": true, "size": 666
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
		    "File:Lanzhousaurus BW.jpg"
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	просто_статья	4350785		35169	20160625015051",
		"2	Lanzhousaurus	6149797		6559	20160623100936",
		"3	Tacit_Fury	6082689		12205	20160623100936"]
	],
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи",
			"text": [
					"<div align=\"center\"><gallery perrow=\"3\" widths=\"125px\" heights=\"125px\" caption=\"Герпетология\">",
					"Файл:Lanzhousaurus BW.jpg|[[Lanzhousaurus]]",
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