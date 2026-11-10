package com.joy.sandbox.core.dungeon.model

/** Пять уровней «Подземелья» — порт js/levels.js: те же координаты, двери и пар. */
object DungeonLevels {

    val ALL: List<DungeonLevel> = listOf(
        DungeonLevel(
            id = 1,
            par = 29,
            name = "Врата темноты",
            w = 4,
            h = 4,
            player = Pos(3, 0),
            enemy = Pos(0, 1),
            hostages = listOf(Pos(0, 3)),
            closed = emptySet(),
            intro = "Главный Хранитель и его дочь прикованы цепями в глубине подземелья. " +
                "Повелитель Тьмы уже чувствует ваши шаги. Изолируйте его — закройте двери, — " +
                "и только потом ведите пленников к выходу в правом верхнем углу.",
        ),
        DungeonLevel(
            id = 2,
            par = 26,
            name = "Затопленные залы",
            w = 4,
            h = 4,
            player = Pos(3, 0),
            enemy = Pos(0, 2),
            hostages = listOf(Pos(0, 3)),
            closed = setOf(DoorId.vertical(1, 0), DoorId.vertical(1, 1), DoorId.vertical(2, 2)),
            intro = "Ураган, насланный Повелителем, обрушил стены: часть дверей заклинило. " +
                "Лабиринт стал теснее — и ловушку придётся ставить изящнее.",
        ),
        DungeonLevel(
            id = 3,
            par = 38,
            name = "Старая темница",
            w = 5,
            h = 5,
            player = Pos(4, 0),
            enemy = Pos(2, 2),
            hostages = listOf(Pos(0, 4)),
            closed = setOf(
                DoorId.horizontal(1, 1),
                DoorId.vertical(2, 2),
                DoorId.horizontal(2, 3),
                DoorId.vertical(3, 0),
            ),
            intro = "Пять рядов каменных залов. Пленник здесь один, но путь к выходу долог, " +
                "а Повелитель не устаёт.",
        ),
        DungeonLevel(
            id = 4,
            par = 52,
            name = "Цепи и ключи",
            w = 5,
            h = 5,
            player = Pos(4, 0),
            enemy = Pos(1, 3),
            hostages = listOf(Pos(0, 4), Pos(0, 0)),
            closed = setOf(
                DoorId.vertical(0, 1),
                DoorId.vertical(0, 2),
                DoorId.horizontal(2, 0),
                DoorId.horizontal(2, 1),
            ),
            intro = "Теперь пленников двое: Хранитель заперт в дальнем углу, а его дочь — " +
                "у самых ворот. Выводите их по очереди, цепочкой друг за другом.",
        ),
        DungeonLevel(
            id = 5,
            par = 38,
            name = "Сердце замка",
            w = 6,
            h = 6,
            player = Pos(5, 0),
            enemy = Pos(2, 2),
            hostages = listOf(Pos(0, 5), Pos(0, 0)),
            closed = setOf(
                DoorId.vertical(1, 1),
                DoorId.horizontal(2, 2),
                DoorId.vertical(3, 3),
                DoorId.horizontal(1, 3),
                DoorId.vertical(4, 1),
                DoorId.horizontal(0, 4),
            ),
            intro = "Самое глубокое подземелье замка. Двое пленников по разным углам, " +
                "искорёженные коридоры — и Повелитель, который знает каждый камень.",
        ),
    )

    fun byId(id: Int): DungeonLevel = ALL.first { it.id == id }
}
