package com.songshuang.sentinel.limit;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.List;

public class LimitRate {

  private LimitRate() {
    initFlowRules();
  }

  private void initFlowRules() {
    List<FlowRule> rules = new ArrayList<>();
    FlowRule rule = new FlowRule();
    rule.setResource("HelloWorld");
    rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
    // Set limit QPS to 20.
    rule.setCount(20);
    rules.add(rule);
    FlowRuleManager.loadRules(rules);

    List<DegradeRule> degradeRules = new ArrayList<DegradeRule>();
    DegradeRule degradeRule = new DegradeRule();
//    degradeRule.setResource(KEY);
    // set threshold rt, 10 ms
    degradeRule.setCount(10);
    degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
    degradeRule.setTimeWindow(10);
    rules.add(rule);
    DegradeRuleManager.loadRules(degradeRules);
  }

  private void limit(final int i) {
    Entry entry = null;
    try {
      entry = SphU.entry("HelloWorld");
      /*您的业务逻辑 - 开始*/
      if (i % 3 == 0) {
        throw new RuntimeException("服务不可用");
      }
      Thread.sleep(5000L);
      System.out.printf("%d : hello world\n", i);
      /*您的业务逻辑 - 结束*/
    } catch (BlockException e1) {
      /*流控逻辑处理 - 开始*/
      System.out.println("block!");
      /*流控逻辑处理 - 结束*/
    }  catch (Exception e) {
    } finally {
      if (entry != null) {
        entry.exit();
      }
    }
  }

  public static void main(String[] args) {
    LimitRate limitRate = new LimitRate();

    for (int i=0; i<20; i++) {
      int finalI = i;
      new Thread(() -> {
        limitRate.limit(finalI);
      }).start();
    }
  }
}
