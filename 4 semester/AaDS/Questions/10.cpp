// Система непересекающихся множеств. Реализация

/*
Система непересекающихся множеств (англ. disjoint-set, или union–find
data structure) — структура данных, которая позволяет администрировать
множество элементов, разбитое на непересекающиеся подмножества.
При этом каждому подмножеству назначается его представитель — элемент
этого подмножества. Абстрактная структура данных определяется
множеством трех операций {Union, Find, MakeSet}.

Условие

Поставим перед собой следующую задачу. Пускай мы оперируем элементами
N видов (для простоты, здесь и далее — числами от 0 до N-1). Некоторые
группы чисел объединены в множества. Также мы можем добавить в структуру
новый элемент, он тем самым образует множество размера 1 из самого себя.
И наконец, периодически некоторые два множества нам потребуется сливать
в одно.
Формализируем задачу: создать быструю структуру, которая поддерживает
следующие операции:

MakeSet(X) — внести в структуру новый элемент X, создать для него множество
размера 1 из самого себя.
Find(X) — возвратить идентификатор множества, которому принадлежит 
элемент X. В качестве идентификатора мы будем выбирать один элемент
из этого множества — представителя множества. Гарантируется, что для
одного и того же множества представитель будет возвращаться один и тот
же, иначе невозможно будет работать со структурой: не будет корректной
даже проверка принадлежности двух элементов одному
множеству if (Find(X) == Find(Y)).
Unite(X, Y) — объединить два множества, в которых лежат элементы
X и Y, в одно новое.

Хранить структуру данных будем в виде леса, то есть превратим DSU в
систему непересекающихся деревьев. Все элементы одного множества лежат
в одном соответствующем дереве, представитель дерева — его корень, слияние
множеств суть просто объединение двух деревьев в одно. Как мы увидим,
такая идея вкупе с двумя небольшими эвристиками ведет к поразительно
высокому быстродействию получившейся структуры.

Для начала нам потребуется массив p, хранящий для каждой вершины дерева
её непосредственного предка (а для корня дерева X — его самого).
С помощью одного только этого массива можно эффективно реализовать
две первые операции DSU:

MakeSet(X):
Чтобы создать новое дерево из элемента X, достаточно указать, что он
является корнем собственного дерева, и предка не имеет.
*/

void MakeSet(int x)
{
	p[x] = x;
}

/*
Find(X)

Представителем дерева будем считать его корень. Тогда для нахождения
этого представителя достаточно подняться вверх по родительским ссылкам
до тех пор, пока не наткнемся на корень.
Но это еще не все: такая наивная реализация в случае вырожденного
(вытянутого в линию) дерева может работать за O(N), что недопустимо.
Можно было бы попытаться ускорить поиск. Например, хранить не только
непосредственного предка, а большие таблицы логарифмического подъема
вверх, но это требует много памяти. Или хранить вместо ссылки на предка
ссылку на собственно корень — однако тогда при слиянии деревьев (Unite)
придется менять эти ссылки всем элементам одного из деревьев, а это
опять-таки временные затраты порядка O(N).
Мы пойдем другим путём: вместо ускорения реализации будем просто
пытаться не допускать чрезмерно длинных веток в дереве. Это первая
эвристика DSU, она называется сжатие путей (path compression).
Суть эвристики: после того, как представитель таки будет найден,
мы для каждой вершины по пути от X к корню изменим предка на этого
самого представителя. То есть фактически переподвесим все эти вершины
вместо длинной ветви непосредственно к корню. Таким образом, реализация
операции Find становится двухпроходной.
*/

int Find(int x)
{
	if (p[x] == x)
		return x;
	return p[x] = Find(p[x]);
}

/*
Unite(X, Y)

Со слиянием двух деревьев придется чуть повозиться. Найдем для начала
корни обоих сливаемых деревьев с помощью уже написанной функции Find.
Теперь, помня, что наша реализация хранит только ссылки на непосредственных
родителей, для слияния деревьев достаточно было бы просто подвесить один
из корней (а с ним и все дерево) сыном к другому. Таким образом все
элементы этого дерева автоматически станут принадлежать другому — и
процедура поиска представителя будет возвращать корень нового дерева. 
Встает вопрос: какое дерево к какому подвешивать? Всегда выбирать
какое-то одно, скажем, дерево X, не годится: легко подобрать пример,
на котором после N объединений мы получим вырожденное дерево — одну
ветку из N элементов. И тут в ход вступает вторая эвристика DSU,
направленная на уменьшение высоты деревьев.
Будем хранить помимо предков еще один массив Rank. В нем для каждого
дерева будет храниться верхняя граница его высоты — то есть длиннейшей
ветви в нем. Заметьте, не сама высота — в процессе выполнения Find
длиннейшая ветвь может самоуничтожиться, а тратить еще итерации на
нахождение новой длиннейшей ветви слишком дорого. Поэтому для каждого
корня в массиве Rank будет записано число, гарантированно больше или
равное высоте его дерева.
Теперь легко принять решении о слиянии: чтобы не допустить слишком
длинных ветвей в DSU, будем подвешивать более низкое дерево к более
высокому. Если их высоты равны — не играет роли, кого подвешивать к
кому. Но в последнем случае новоиспеченному корню надо не забыть
увеличить Rank.
*/

void Unite(int x, int y)
{
	x = Find(x);
	y = Find(y);

	if (rank[x] < rank[y])
		p[x] = y
	else
	{
		p[y] = x;
		if (rank[x] == rank[y])
			++rank[x];
	}
}

/*
MakeSet(X) — O(1).
Find(X) — в принципе O(1).
Unite(X, Y) — в принципе O(1).
Расход памяти — O(N).
*/