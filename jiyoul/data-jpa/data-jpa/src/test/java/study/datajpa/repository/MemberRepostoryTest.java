package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class MemberRepostoryTest {

    @Autowired
    MemberRepostory memberRepostory;
    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("membarA");
        Member savedMember = memberRepostory.save(member);

        Member findMember = memberRepostory.findById(savedMember.getId()).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepostory.save(member1);
        memberRepostory.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepostory.findById(member1.getId()).get();
        Member findMember2 = memberRepostory.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepostory.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepostory.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepostory.delete(member1);
        memberRepostory.delete(member2);

        long deletedCount = memberRepostory.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepostory.save(m1);
        memberRepostory.save(m2);

        List<Member> result = memberRepostory.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy() {
        List<Member> helloBy = memberRepostory.findTop3HelloBy();
    }

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepostory.save(m1);
        memberRepostory.save(m2);

        List<Member> result = memberRepostory.findByUsername("AAA");

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepostory.save(m1);
        memberRepostory.save(m2);

        List<Member> result = memberRepostory.findUser("AAA", 10);

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUserNameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepostory.save(m1);
        memberRepostory.save(m2);

        List<String> usernameList = memberRepostory.findUsernameList();

        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepostory.save(m1);

        List<MemberDto> memberDto = memberRepostory.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepostory.save(m1);
        memberRepostory.save(m2);

        List<Member> result = memberRepostory.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepostory.save(m1);
        memberRepostory.save(m2);

        Optional<Member> findMember = memberRepostory.findOptionalByUsername("AAA");
        System.out.println("findMember = " + findMember);
    }

    //페이징 조건과 정렬 조건 설정
    @Test
    public void page() throws Exception {
        //given
        memberRepostory.save(new Member("member1", 10));
        memberRepostory.save(new Member("member2", 10));
        memberRepostory.save(new Member("member3", 10));
        memberRepostory.save(new Member("member4", 10));
        memberRepostory.save(new Member("member5", 10));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepostory.findByAge(10, pageRequest);
        Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        //then
        List<Member> content = page.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() throws Exception {
        //given
        memberRepostory.save(new Member("member1", 10));
        memberRepostory.save(new Member("member2", 19));
        memberRepostory.save(new Member("member3", 20));
        memberRepostory.save(new Member("member4", 21));
        memberRepostory.save(new Member("member5", 40));

        //when
        int resultCount = memberRepostory.bulkAgePlus(20);
//        em.flush();
//        em.clear(); Bulk 연산 실행후엔 영속성 컨텍스트를 초기화 해주는게 좋다.

        List<Member> result = memberRepostory.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        //then
        assertThat(resultCount).isEqualTo(3);
    }
}
