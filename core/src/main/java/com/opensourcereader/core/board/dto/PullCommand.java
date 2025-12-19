package com.opensourcereader.core.board.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PullCommand extends BoardBaseCommand {

  private Long reviewCount;
}
