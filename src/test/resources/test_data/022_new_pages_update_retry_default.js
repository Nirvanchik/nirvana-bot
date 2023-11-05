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
                    "}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": null
            }
        ],
        "firstRevision": [
            {
                "revid": 688, "timestamp": 1275807366, "title": "Розовый пудель", "summary": "create new page",
                "user" : "MegaUser 6", "minor": false, "bot": false, "rvnew": true, "size": 1500
            }, {
                "revid": 690, "timestamp": 1275808366, "title": "Белый пудель", "summary": "create new page",
                "user" : "MegaUser 7", "minor": false, "bot": false, "rvnew": true, "size": 50
            }
        ],
		"topRevision": [
			{
				"title": "Проект:Project 1/Новые статьи"
			} 
		]
    },
    "wiki_tools" : [
        "@java.net.SocketTimeoutException",
        ["number	title	pageid	namespace	length	touched",
         "1	Белый_пудель	690		25460	20160603012302",
         "2	Розовый_пудель	688		18924	20160603134155"]
    ],
    "expected_edits": [
        {
            "title": "Проект:Project 1/Новые статьи",
            "text": [
                "* [[Белый пудель]]",
                "* [[Розовый пудель]]"],
            "section": -2
        }
    ],
    "expected_tools_queries": [
        {
            "contains": ["Собаки"]
        },
    {
        "contains": ["Собаки"]
    }
    ]
}