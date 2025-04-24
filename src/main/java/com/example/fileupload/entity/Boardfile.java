package com.example.fileupload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "boardfile")
public class Boardfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int fno;
	@Column(name = "ftype")
	private String ftype;
	@Column(name = "foriginname")
	private String foriginname;
	@Column(name = "fname")
	private String fname;
	@Column(name = "fext")
	private String fext;
	@Column(name = "fsize")
	private long fsize;
	
	// 사용하지 않ㄴ는 연관 관계 설정 주석처리
	// 자식에서 부모로 단방향 관계설정 O
	// 불필요한 연관 관계는 조인 등으로 인해 시스템 부하를 가져옴 -> 실제 조인이 필요한 비즈니스 로직이 있을때만 연관 관계 설정
	// @ManyToOne // 관계 설정
	// @JoinColumn(name = "bno")// FK
	@Column(name = "bno")
	private int bno;
}
