package top.naccl.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @Description: 最新推荐博客
 * @Author: Naccl
 * @Date: 2020-09-05
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NewBlog {
	private Long id;
	private String title;
	private String password;
	private Boolean privacy;
    private Date createTime;
}
