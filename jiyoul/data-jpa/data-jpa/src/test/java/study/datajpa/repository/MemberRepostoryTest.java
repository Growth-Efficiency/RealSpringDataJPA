package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class MemberRepostoryTest {

    @Autowired
    MemberRepostory memberRepostory;

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
}