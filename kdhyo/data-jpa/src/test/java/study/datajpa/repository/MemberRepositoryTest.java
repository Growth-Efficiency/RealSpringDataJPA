package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	TeamRepository teamRepository;

	@PersistenceContext
	EntityManager em;

	@Test
	void testMember() {
		// given
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		// when
		Member findMember = memberRepository.findById(savedMember.getId()).get();

		// then
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(savedMember);
	}

	@Test
	void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		List<Member> all = memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);

		memberRepository.delete(member1);
		memberRepository.delete(member2);

		long deletedCount = memberRepository.count();
		assertThat(deletedCount).isZero();
	}

	@Test
	void findByUsernameAndAgeGreaterThen() {
		// given
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		// when
		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

		// then
		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	void testQuery() {
		// given
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		// when
		List<Member> result = memberRepository.findUser("AAA", 10);

		// then
		assertThat(result.get(0)).isEqualTo(m1);
	}

	@Test
	void findUsernameList() {
		// given
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		// when
		List<String> usernameList = memberRepository.findUsernameList();

		// then
		for (String s : usernameList) {
			System.out.println("s = " + s);
		}
	}

	@Test
	void findMemberDto() {
		// given
		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.changeTeam(team);
		memberRepository.save(m1);

		// when
		List<MemberDto> memberDto = memberRepository.findMemberDto();

		// then
		for (MemberDto dto : memberDto) {
			System.out.println("dto = " + dto);
		}
	}

	@Test
	void paging() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));

		// when
		Page<Member> page = memberRepository.findByAge(age, pageRequest);

		// then
		List<Member> content = page.getContent();
		long totalElements = page.getTotalElements();

		assertThat(content).hasSize(3);
		assertThat(totalElements).isEqualTo(5);
		assertThat(page.getNumber()).isZero();
		assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}

	@Test
	void bulkUpdate() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		// when
		int resultCount = memberRepository.bulkAgePlus(20);

		// then
		assertThat(resultCount).isEqualTo(3);
	}

	@Test
	void findMemberLazy() {
		// given
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member m1 = new Member("member1", 10, teamA);
		Member m2 = new Member("member2", 10, teamB);
		memberRepository.save(m1);
		memberRepository.save(m2);

		em.flush();
		em.clear();

		// when
		List<Member> members = memberRepository.findMemberEntityGraph();

		// then
		for (Member member : members) {
			System.out.println("member.username = " + member.getUsername());
			System.out.println("member.teamName = " + member.getTeam().getName());
		}
	}

	@Test
	void queryHint() {
	    // given
		Member m1 = new Member("member1", 10);
		memberRepository.save(m1);
		em.flush();
		em.clear();

	    // when
		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2"); // readonly 활성화 상태여서 update query 이 발생되지 않음.

		em.flush();
		// then
	}

}
