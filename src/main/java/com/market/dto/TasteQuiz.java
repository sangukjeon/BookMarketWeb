package com.market.dto;

import java.util.List;

/** 독서 성향 테스트 질문 5개. 보기마다 곰 유형 하나에 1점 */
public final class TasteQuiz {
	public record Option(String text, BearType type) {
		public String getText() { return text; }
		public String getValue() { return type.getSlug(); }
	}

	public record Question(String text, List<Option> options) {
		public String getText() { return text; }
		public List<Option> getOptions() { return options; }
	}

	public static final List<Question> QUESTIONS = List.of(
			new Question("주말 오후, 책을 펼친다면 어디에서?", List.of(
					new Option("기차 창가 자리", BearType.ADVENTURE),
					new Option("조용한 도서관 구석", BearType.THINKER),
					new Option("아침 일찍 카페", BearType.GROWER),
					new Option("이불 속 침대 위", BearType.FEELER),
					new Option("노트북 옆 책상", BearType.MAKER))),
			new Question("책을 고를 때 가장 먼저 보는 것은?", List.of(
					new Option("줄거리가 궁금한지", BearType.ADVENTURE),
					new Option("새로 알게 될 지식", BearType.THINKER),
					new Option("바로 써먹을 수 있는지", BearType.GROWER),
					new Option("표지와 문장의 분위기", BearType.FEELER),
					new Option("따라 할 예제가 있는지", BearType.MAKER))),
			new Question("친구가 책 추천을 부탁하면?", List.of(
					new Option("밤새 읽은 소설", BearType.ADVENTURE),
					new Option("생각할 거리를 준 책", BearType.THINKER),
					new Option("습관을 바꿔 준 책", BearType.GROWER),
					new Option("마음이 따뜻해진 책", BearType.FEELER),
					new Option("뭔가 만들게 해 준 책", BearType.MAKER))),
			new Question("다 읽고 나서 나는…", List.of(
					new Option("다음 권부터 찾는다", BearType.ADVENTURE),
					new Option("관련된 글을 더 찾아본다", BearType.THINKER),
					new Option("할 일 목록을 쓴다", BearType.GROWER),
					new Option("좋은 문장을 옮겨 적는다", BearType.FEELER),
					new Option("직접 해 본다", BearType.MAKER))),
			new Question("요즘 나에게 필요한 건?", List.of(
					new Option("일상에서 잠깐 벗어나기", BearType.ADVENTURE),
					new Option("세상을 보는 새 눈", BearType.THINKER),
					new Option("더 나은 내일의 나", BearType.GROWER),
					new Option("위로와 웃음", BearType.FEELER),
					new Option("새 기술 하나", BearType.MAKER))));

	private TasteQuiz() {
	}

	/** 답(질문 순서대로 곰 slug)으로 결과를 정한다. 동점이면 enum 순서가 앞선 쪽. 답이 모자라면 null */
	public static BearType score(List<String> answers) {
		if (answers == null || answers.size() != QUESTIONS.size())
			return null;
		int[] score = new int[BearType.values().length];
		for (int i = 0; i < answers.size(); i++) {
			BearType t = BearType.bySlug(answers.get(i));
			if (t == null)
				return null;
			final BearType chosen = t;
			if (QUESTIONS.get(i).options().stream().noneMatch(o -> o.type() == chosen))
				return null;
			score[t.ordinal()]++;
		}
		int best = 0;
		for (int i = 1; i < score.length; i++)
			if (score[i] > score[best])
				best = i;
		return BearType.values()[best];
	}
}
