package com.lambdaschool.gdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController

public class GdpController
{
    private final GdpRepository gdprepos;
    private final RabbitTemplate rt;

    public GdpController(GdpRepository gdprepos, RabbitTemplate rt)
    {
        this.gdprepos = gdprepos;
        this.rt = rt;
    }

    @GetMapping("/names")
    public List<GDP> names()
    {
        List<GDP> gdpList = gdprepos.findAll();
//        gdpList.forEach(n -> System.out.println(n.getCountry()));
        gdpList.sort(Comparator.comparing(GDP::getCountry));
        return gdpList;
    }

    @GetMapping("/economy")
    public List<GDP> economy()
    {
        List<GDP> gdpList = gdprepos.findAll();
        gdpList.sort((g1, g2) -> (int)(g2.getGdp() - g1.getGdp()));
        return gdpList;
    }

    @GetMapping("/total")
    public ObjectNode total()
    {
        List<GDP> gdpList = gdprepos.findAll();
        Long total = 0L;
        for (GDP g : gdpList)
        {
            total += g.getGdp();
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode totalGDP = mapper.createObjectNode();
        totalGDP.put("id", 0);
        totalGDP.put("country", "total");
        totalGDP.put("gdp", total);

        return totalGDP;
    }

    @GetMapping("/gdp/{name}")
    public GDP findGDP(@PathVariable String name)
    {
        List<GDP> gdpList = gdprepos.findAll();
        GDP gdp = new GDP("Not Found", 0L);
        for(GDP g : gdpList)
        {
            if (g.getCountry().equals(name))
            {
                gdp = g;
            }
        }

        GdpLog message = new GdpLog("Checked countries by name");
        rt.convertAndSend(GdpApplication.QUEUE_NAME, message.toString());
        log.info("Message Sent");
        return gdp;
    }

    @PostMapping("/gdp")
    public List<GDP> gdp(@RequestBody List<GDP> newCountries)
    {
        return gdprepos.saveAll(newCountries);
    }
}
